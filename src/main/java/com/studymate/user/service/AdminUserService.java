package com.studymate.user.service;

import com.studymate.user.dto.AdminCreateUserRequest;
import com.studymate.user.dto.AdminUserDto;
import com.studymate.user.entity.Role;
import com.studymate.user.entity.User;
import com.studymate.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    public AdminUserService(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDto> findUsers(String keyword, Role role, Boolean enabled, Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        return userRepository.searchUsers(normalizedKeyword, role, enabled, pageable);
    }

    @Transactional
    public boolean toggleUserEnabled(Long targetUserId, Long currentAdminId) {
        if (Objects.equals(targetUserId, currentAdminId)) {
            throw new IllegalArgumentException("Không thể tự khóa tài khoản của chính mình");
        }
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        boolean nextEnabled = !user.isEnabled();
        user.setEnabled(nextEnabled);
        userRepository.save(user);
        activityLogService.record(currentAdminId, user.getEmail(), "ADMIN_TOGGLE_STATUS",
                nextEnabled ? "Mở khóa tài khoản" : "Khóa tài khoản");
        return nextEnabled;
    }

    @Transactional
    public void changeUserRole(Long targetUserId, Role newRole, Long currentAdminId) {
        if (newRole == null) {
            throw new IllegalArgumentException("Vai trò không hợp lệ");
        }
        if (Objects.equals(targetUserId, currentAdminId) && newRole != Role.ADMIN) {
            throw new IllegalArgumentException("Không thể tự thay đổi vai trò của chính mình");
        }
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        Role previousRole = user.getRole();
        user.setRole(newRole);
        userRepository.save(user);
        activityLogService.record(currentAdminId, user.getEmail(), "ADMIN_CHANGE_ROLE",
                "Đổi vai trò từ " + previousRole.name() + " sang " + newRole.name());
    }

    @Transactional
    public User createUser(AdminCreateUserRequest request, Long currentAdminId) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Mật khẩu tối đa 72 byte");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email này đã tồn tại trong hệ thống");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setRole(request.getRole());
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);

        activityLogService.record(currentAdminId, saved.getEmail(), "ADMIN_CREATE_USER",
                "Tạo tài khoản mới với vai trò " + saved.getRole().name());
        return saved;
    }
}
