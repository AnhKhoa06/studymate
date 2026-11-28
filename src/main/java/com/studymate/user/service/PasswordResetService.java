package com.studymate.user.service;

import com.studymate.user.entity.PasswordResetToken;
import com.studymate.user.entity.User;
import com.studymate.user.repository.PasswordResetTokenRepository;
import com.studymate.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_EXPIRY_MINUTES = 30;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final ActivityLogService activityLogService;

    @Value("${spring.mail.from:no-reply@studymate.local}")
    private String mailFrom;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                ObjectProvider<JavaMailSender> mailSenderProvider,
                                ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSenderProvider = mailSenderProvider;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public void requestPasswordReset(String email, String resetBaseUrl) {
        if (email == null || email.isBlank()) {
            return;
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalizedEmail).orElse(null);
        if (user == null || !user.isEnabled()) {
            // Khong tiet lo email co ton tai hay khong
            return;
        }

        String rawToken = generateToken();
        PasswordResetToken entity = new PasswordResetToken();
        entity.setUser(user);
        entity.setTokenHash(sha256(rawToken));
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
        entity.setUsed(false);
        entity.setCreatedAt(LocalDateTime.now());
        tokenRepository.save(entity);

        String resetUrl = resetBaseUrl + "?token=" + rawToken;
        sendResetEmail(user.getEmail(), user.getFullName(), resetUrl);
        activityLogService.record(user.getId(), user.getEmail(), "PASSWORD_RESET_REQUEST",
                "Yêu cầu đặt lại mật khẩu qua email");
    }

    @Transactional(readOnly = true)
    public boolean isValidToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }
        return tokenRepository.findByTokenHash(sha256(rawToken.trim()))
                .map(PasswordResetToken::isValidNow)
                .orElse(false);
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword, String confirmPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Liên kết đặt lại mật khẩu không hợp lệ");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu tối thiểu 8 ký tự");
        }
        if (newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Mật khẩu tối đa 72 byte");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu nhập lại không khớp");
        }

        PasswordResetToken token = tokenRepository.findByTokenHash(sha256(rawToken.trim()))
                .orElseThrow(() -> new IllegalArgumentException("Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn"));

        if (!token.isValidNow()) {
            throw new IllegalArgumentException("Liên kết đặt lại mật khẩu đã hết hạn hoặc đã được sử dụng");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        activityLogService.record(user.getId(), user.getEmail(), "PASSWORD_RESET_SUCCESS",
                "Đặt lại mật khẩu thành công");
    }

    private void sendResetEmail(String toEmail, String fullName, String resetUrl) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Chua cau hinh JavaMailSender; bo qua gui email dat lai mat khau cho {}", toEmail);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(toEmail);
            message.setSubject("[StudyMate] Đặt lại mật khẩu");
            message.setText("Xin chào " + fullName + ",\n\n"
                    + "Nhấn vào liên kết sau để đặt lại mật khẩu (có hiệu lực trong "
                    + TOKEN_EXPIRY_MINUTES + " phút):\n" + resetUrl + "\n\n"
                    + "Nếu bạn không yêu cầu thao tác này, vui lòng bỏ qua email.");
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Gui email dat lai mat khau that bai cho {}: {}", toEmail, ex.getMessage());
        }
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
