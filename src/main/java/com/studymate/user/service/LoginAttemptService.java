package com.studymate.user.service;

import com.studymate.common.security.CurrentUser;
import com.studymate.user.entity.User;
import com.studymate.user.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class LoginAttemptService {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public LoginAttemptService(UserRepository userRepository, ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    @EventListener
    @Transactional
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (!(principal instanceof String email) || email.isBlank()) {
            return;
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        userRepository.findByEmail(normalizedEmail).ifPresent(user -> {
            int nextAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(nextAttempts);
            if (nextAttempts >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                activityLogService.record(user.getId(), user.getEmail(), "ACCOUNT_LOCKED_TEMP",
                        "Khóa tạm " + LOCK_DURATION_MINUTES + " phút do đăng nhập sai " + nextAttempts + " lần");
            } else {
                activityLogService.record(user.getId(), user.getEmail(), "LOGIN_FAILED",
                        "Đăng nhập sai mật khẩu (lần " + nextAttempts + ")");
            }
            userRepository.save(user);
        });
    }

    @EventListener
    @Transactional
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (!(principal instanceof CurrentUser currentUser)) {
            return;
        }
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        if (user == null) {
            return;
        }
        if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
        activityLogService.record(user.getId(), user.getEmail(), "LOGIN_SUCCESS", "Đăng nhập thành công");
    }
}
