package com.studymate.user;

import com.studymate.common.security.CurrentUser;
import com.studymate.user.entity.PasswordResetToken;
import com.studymate.user.entity.Role;
import com.studymate.user.entity.User;
import com.studymate.user.repository.ActivityLogRepository;
import com.studymate.user.repository.PasswordResetTokenRepository;
import com.studymate.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityEnhancementsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
        activityLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void failedLoginsExceedingThreshold_locksAccountTemporarilyAndLogsEvent() throws Exception {
        User student = createUser("lock.test@studymate.test", "Lock User", Role.STUDENT);

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/login")
                            .with(csrf())
                            .param("username", student.getEmail())
                            .param("password", "WrongPass!"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?error"));
        }

        User reloaded = userRepository.findById(student.getId()).orElseThrow();
        assertThat(reloaded.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(reloaded.isTemporarilyLocked()).isTrue();

        // Lan thu thu 6 du dung mat khau van bi chan voi ?locked
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", student.getEmail())
                        .param("password", "Secret123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?locked"));

        assertThat(activityLogRepository.findAll())
                .anyMatch(l -> "ACCOUNT_LOCKED_TEMP".equals(l.getAction()));
    }

    @Test
    void adminLogs_onlyAccessibleByAdmin() throws Exception {
        User student = createUser("log.student@studymate.test", "SV", Role.STUDENT);
        User admin = createUser("log.admin@studymate.test", "Admin", Role.ADMIN);

        mockMvc.perform(get("/admin/logs").with(user(toPrincipal(student))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/logs").with(user(toPrincipal(admin))))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("logsPage"));
    }

    @Test
    void forgotAndResetPassword_updatesPasswordAndUnlocksUser() throws Exception {
        User student = createUser("reset.test@studymate.test", "Reset User", Role.STUDENT);
        student.setFailedLoginAttempts(5);
        student.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        userRepository.save(student);

        mockMvc.perform(get("/forgot-password")).andExpect(status().isOk());
        mockMvc.perform(post("/forgot-password")
                        .with(csrf())
                        .param("email", student.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/forgot-password"));

        String rawToken = "test-reset-token-12345";
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(student);
        token.setTokenHash(sha256(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        token.setUsed(false);
        token.setCreatedAt(LocalDateTime.now());
        tokenRepository.save(token);

        mockMvc.perform(get("/reset-password").param("token", rawToken))
                .andExpect(status().isOk())
                .andExpect(model().attribute("tokenValid", true));

        mockMvc.perform(post("/reset-password")
                        .with(csrf())
                        .param("token", rawToken)
                        .param("newPassword", "NewSecret123!")
                        .param("confirmPassword", "NewSecret123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?reset"));

        User updated = userRepository.findById(student.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("NewSecret123!", updated.getPasswordHash())).isTrue();
        assertThat(updated.getFailedLoginAttempts()).isZero();
        assertThat(updated.isTemporarilyLocked()).isFalse();
    }

    private User createUser(String email, String fullName, Role role) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode("Secret123!"));
        u.setFullName(fullName);
        u.setRole(role);
        u.setEnabled(true);
        u.setCreatedAt(LocalDateTime.now());
        return userRepository.save(u);
    }

    private CurrentUser toPrincipal(User u) {
        return new CurrentUser(
                u.getId(), u.getEmail(), u.getPasswordHash(), u.getFullName(),
                u.getRole(), u.isEnabled(),
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
        );
    }

    private static String sha256(String raw) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
    }
}
