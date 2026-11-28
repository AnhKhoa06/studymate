package com.studymate.user.service;

import com.studymate.user.dto.RegisterRequest;
import com.studymate.user.entity.Role;
import com.studymate.user.entity.User;
import com.studymate.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;
    }

    // Dang ky tai khoan moi. Luon tra ve STUDENT, bo qua moi truong role trong request.
    @Transactional
    public User register(RegisterRequest request) {
        // Chuan hoa email: trim va chuyen chu thuong
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

        // Kiem tra do dai mat khau theo byte (BCrypt gioi han 72 byte UTF-8, tu choi truoc)
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new PasswordTooLongException("Mật khẩu tối đa 72 byte (khoảng 24 ký tự có dấu hoặc 72 ký tự ASCII)");
        }

        // Kiem tra mat khau khop
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Mật khẩu nhập lại không khớp");
        }

        // Kiem tra email trung (khong tiet lo co ton tai hay khong trong thong bao chung)
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email này đã được sử dụng");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setRole(Role.STUDENT); // luon STUDENT, khong lay tu request
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT));
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu mới tối thiểu 8 ký tự");
        }
        if (newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Mật khẩu mới tối đa 72 byte");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu nhập lại không khớp");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        activityLogService.record(userId, user.getEmail(), "PASSWORD_CHANGED", "Người dùng tự đổi mật khẩu");
    }

    // Exception noi bo, khai bao o day de gon
    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class PasswordMismatchException extends RuntimeException {
        public PasswordMismatchException(String message) {
            super(message);
        }
    }

    public static class PasswordTooLongException extends RuntimeException {
        public PasswordTooLongException(String message) {
            super(message);
        }
    }
}
