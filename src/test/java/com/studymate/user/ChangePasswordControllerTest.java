package com.studymate.user;

import com.studymate.common.security.CurrentUser;
import com.studymate.user.entity.Role;
import com.studymate.user.entity.User;
import com.studymate.user.repository.ActivityLogRepository;
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

import java.time.LocalDateTime;
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
class ChangePasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        activityLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getChangePassword_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/change-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void postChangePassword_validRequest_updatesPasswordHash() throws Exception {
        User saved = createUser("student.pw@studymate.test", "Sinh Vien", Role.STUDENT, "OldPass123!");
        CurrentUser principal = toPrincipal(saved);

        mockMvc.perform(post("/change-password")
                        .with(user(principal))
                        .with(csrf())
                        .param("currentPassword", "OldPass123!")
                        .param("newPassword", "NewPass456!")
                        .param("confirmPassword", "NewPass456!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/change-password"));

        User reloaded = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("NewPass456!", reloaded.getPasswordHash())).isTrue();
    }

    @Test
    void postChangePassword_wrongCurrentPassword_returnsErrors() throws Exception {
        User saved = createUser("student.pw2@studymate.test", "Sinh Vien 2", Role.STUDENT, "OldPass123!");
        CurrentUser principal = toPrincipal(saved);

        mockMvc.perform(post("/change-password")
                        .with(user(principal))
                        .with(csrf())
                        .param("currentPassword", "WrongPass!")
                        .param("newPassword", "NewPass456!")
                        .param("confirmPassword", "NewPass456!"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());

        User reloaded = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("OldPass123!", reloaded.getPasswordHash())).isTrue();
    }

    @Test
    void postProfile_managerChangesPasswordInline_updatesNameAndPassword() throws Exception {
        User manager = createUser("manager.pw@studymate.test", "Quan Ly", Role.MANAGER, "OldPass123!");
        CurrentUser principal = toPrincipal(manager);

        mockMvc.perform(post("/profile")
                        .with(user(principal))
                        .with(csrf())
                        .param("fullName", "Quan Ly Moi")
                        .param("bio", "Ghi chu moi")
                        .param("currentPassword", "OldPass123!")
                        .param("newPassword", "NewManager456!")
                        .param("confirmPassword", "NewManager456!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        User reloaded = userRepository.findById(manager.getId()).orElseThrow();
        assertThat(reloaded.getFullName()).isEqualTo("Quan Ly Moi");
        assertThat(passwordEncoder.matches("NewManager456!", reloaded.getPasswordHash())).isTrue();
    }

    private User createUser(String email, String fullName, Role role, String rawPassword) {
        User u = new User();
        u.setEmail(email);
        u.setFullName(fullName);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setEnabled(true);
        u.setCreatedAt(LocalDateTime.now());
        return userRepository.save(u);
    }

    private CurrentUser toPrincipal(User u) {
        return new CurrentUser(
                u.getId(),
                u.getEmail(),
                u.getPasswordHash(),
                u.getFullName(),
                u.getRole(),
                u.isEnabled(),
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
        );
    }
}
