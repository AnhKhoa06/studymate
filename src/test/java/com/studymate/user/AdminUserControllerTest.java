package com.studymate.user;

import com.studymate.common.security.CurrentUser;
import com.studymate.user.entity.Role;
import com.studymate.user.entity.User;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void adminUsers_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void adminUsers_nonAdminRole_returns403() throws Exception {
        User student = createUser("student.acl@studymate.test", "Sinh Vien", Role.STUDENT);
        User manager = createUser("manager.acl@studymate.test", "Quan Ly", Role.MANAGER);

        mockMvc.perform(get("/admin/users").with(user(toPrincipal(student))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/users").with(user(toPrincipal(manager))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminUsers_adminCanListFilterToggleAndChangeRole() throws Exception {
        User admin = createUser("admin.panel@studymate.test", "Admin", Role.ADMIN);
        User target = createUser("target.user@studymate.test", "Target Student", Role.STUDENT);
        CurrentUser adminPrincipal = toPrincipal(admin);

        mockMvc.perform(get("/admin/users").param("q", "target").with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("usersPage"));

        mockMvc.perform(post("/admin/users/" + target.getId() + "/toggle-status")
                        .with(user(adminPrincipal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        assertThat(userRepository.findById(target.getId()).orElseThrow().isEnabled()).isFalse();

        mockMvc.perform(post("/admin/users/" + target.getId() + "/role")
                        .with(user(adminPrincipal))
                        .with(csrf())
                        .param("newRole", Role.MANAGER.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        assertThat(userRepository.findById(target.getId()).orElseThrow().getRole()).isEqualTo(Role.MANAGER);
    }

    @Test
    void adminUsers_cannotLockOrDemoteSelf() throws Exception {
        User admin = createUser("admin.self@studymate.test", "Admin Self", Role.ADMIN);
        CurrentUser adminPrincipal = toPrincipal(admin);

        mockMvc.perform(post("/admin/users/" + admin.getId() + "/toggle-status")
                        .with(user(adminPrincipal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));

        mockMvc.perform(post("/admin/users/" + admin.getId() + "/role")
                        .with(user(adminPrincipal))
                        .with(csrf())
                        .param("newRole", Role.STUDENT.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));

        User reloaded = userRepository.findById(admin.getId()).orElseThrow();
        assertThat(reloaded.isEnabled()).isTrue();
        assertThat(reloaded.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void adminUsers_adminCanCreateManagerAccountDirectly() throws Exception {
        User admin = createUser("admin.creator@studymate.test", "Admin Creator", Role.ADMIN);
        CurrentUser adminPrincipal = toPrincipal(admin);

        mockMvc.perform(post("/admin/users/create")
                        .with(user(adminPrincipal))
                        .with(csrf())
                        .param("fullName", "New Manager")
                        .param("email", "new.manager@studymate.test")
                        .param("password", "Manager123!")
                        .param("role", Role.MANAGER.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        User created = userRepository.findByEmail("new.manager@studymate.test").orElseThrow();
        assertThat(created.getRole()).isEqualTo(Role.MANAGER);
        assertThat(created.isEnabled()).isTrue();
        assertThat(passwordEncoder.matches("Manager123!", created.getPasswordHash())).isTrue();
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
