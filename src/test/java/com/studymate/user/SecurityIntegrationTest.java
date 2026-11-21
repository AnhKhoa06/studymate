package com.studymate.user;

import com.studymate.user.entity.Role;
import com.studymate.user.entity.User;
import com.studymate.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    private static final String LOGIN_ERROR_MESSAGE = "Thông tin đăng nhập không chính xác";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Chi xoa du lieu trong DB test (studymate_oop_test), khong bao gio tro vao DB that
    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    private User createSampleUser(String email, String rawPassword, Role role, boolean enabled) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName("Test User");
        user.setRole(role);
        user.setEnabled(enabled);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Test
    @DisplayName("(1) Chưa đăng nhập vào / bị chuyển hướng về /login")
    void test1_unauthenticatedAccessToHomeRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("(2) POST /register không có CSRF token bị từ chối 403 và không tạo tài khoản")
    void test2_postRegisterWithoutCsrfIsForbidden() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "student1@example.com")
                        .param("fullName", "Nguyen Van A")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isForbidden());

        assertThat(userRepository.findByEmail("student1@example.com")).isEmpty();
    }

    @Test
    @DisplayName("(3a) STUDENT vào /admin/ping bị 403")
    @WithMockUser(username = "student@test.local", roles = {"STUDENT"})
    void test3a_studentAccessAdminPingForbidden() throws Exception {
        mockMvc.perform(get("/admin/ping"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("(3b) STUDENT vào /manager/ping bị 403")
    @WithMockUser(username = "student@test.local", roles = {"STUDENT"})
    void test3b_studentAccessManagerPingForbidden() throws Exception {
        mockMvc.perform(get("/manager/ping"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("(3c) ADMIN vào /admin/ping được 200")
    @WithMockUser(username = "admin@test.local", roles = {"ADMIN"})
    void test3c_adminAccessAdminPingOk() throws Exception {
        mockMvc.perform(get("/admin/ping"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("(4) Đăng ký thành công: băm BCrypt, role luôn là STUDENT kể cả khi gửi role=ADMIN")
    void test4_registerSuccessAlwaysStudentWithBcrypt() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "newbie@example.com")
                        .param("fullName", "Tran Thi B")
                        .param("password", "strongPass123")
                        .param("confirmPassword", "strongPass123")
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        User savedUser = userRepository.findByEmail("newbie@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("strongPass123");
        assertThat(savedUser.getPasswordHash()).startsWith("$2");
        assertThat(savedUser.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    @DisplayName("(5) Đăng ký với email trùng (khác hoa thường) bị báo lỗi tại trường email")
    void test5_registerWithDuplicateEmailDifferentCasingRejected() throws Exception {
        createSampleUser("unique@example.com", "pass12345", Role.STUDENT, true);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "UNIQUE@EXAMPLE.COM")
                        .param("fullName", "User Khac")
                        .param("password", "pass12345")
                        .param("confirmPassword", "pass12345"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registerRequest", "email"))
                .andExpect(content().string(containsString("Email này đã được sử dụng")));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("(6) Tài khoản enabled=false không đăng nhập được")
    void test6_disabledAccountCannotLogin() throws Exception {
        createSampleUser("locked@example.com", "validPass123", Role.STUDENT, false);

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "locked@example.com")
                        .param("password", "validPass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("(7) Đăng nhập thất bại (email lạ, sai pass, khóa) đều cùng kết quả và cùng thông báo")
    void test7_loginFailuresProduceIdenticalOutcome() throws Exception {
        createSampleUser("active@example.com", "correctPass123", Role.STUDENT, true);
        createSampleUser("inactive@example.com", "secretPass123", Role.STUDENT, false);

        String[][] attempts = {
                {"nonexistent@example.com", "anyPassword"},   // email la
                {"active@example.com", "wrongPassword"},      // sai mat khau
                {"inactive@example.com", "secretPass123"},    // khoa, mat khau dung
                {"inactive@example.com", "incorrectPass"}     // khoa, mat khau sai
        };

        for (String[] attempt : attempts) {
            mockMvc.perform(post("/login").with(csrf())
                            .param("username", attempt[0])
                            .param("password", attempt[1]))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?error"))
                    .andExpect(unauthenticated());
        }

        // Trang dang nhap luon hien dung mot cau co dinh, khong tiet lo nguyen nhan
        mockMvc.perform(get("/login").param("error", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(LOGIN_ERROR_MESSAGE)));
    }

    @Test
    @DisplayName("(8) Mật khẩu quá 72 byte UTF-8 (nhưng chưa quá 72 ký tự) bị từ chối ở trường password")
    void test8_passwordLongerThan72BytesRejected() throws Exception {
        // 30 ky tu, moi ky tu 3 byte UTF-8 = 90 byte (> 72 byte, nhung < 72 ky tu)
        String longPass = "\u1EBF".repeat(30);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "longpass@example.com")
                        .param("fullName", "Tran Van C")
                        .param("password", longPass)
                        .param("confirmPassword", longPass))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registerRequest", "password"));

        assertThat(userRepository.findByEmail("longpass@example.com")).isEmpty();
    }

    @Test
    @DisplayName("(9) Email hợp lệ về định dạng nhưng dài hơn 255 ký tự bị từ chối bởi @Size, không trả 500")
    void test9_emailLongerThan255CharactersRejected() throws Exception {
        // Phan truoc @ = 64 ky tu, moi nhan ten mien = 60 ky tu: dung dinh dang, tong > 255
        String label = "abcdefghij".repeat(6);
        String longEmail = "a".repeat(64) + "@" + String.join(".", label, label, label, label) + ".com";
        assertThat(longEmail.length()).isGreaterThan(255);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", longEmail)
                        .param("fullName", "Pham Van D")
                        .param("password", "matkhau123")
                        .param("confirmPassword", "matkhau123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("registerRequest", "email", "Size"));
    }

    @Test
    @DisplayName("(10) Đăng ký không điền email hoặc họ tên thì hiện lỗi từng trường, không trả 500")
    void test10_emptyEmailOrNameHasFieldErrorsNot500() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "")
                        .param("fullName", "")
                        .param("password", "matkhau123")
                        .param("confirmPassword", "matkhau123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registerRequest", "email"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "fullName"));

        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("(11) Đăng ký với email có khoảng trắng hai đầu thì thành công và email lưu đã được cắt")
    void test11_registerWithWhitespaceEmailSavedTrimmed() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "   Trimmed.User@Example.com   ")
                        .param("fullName", "User Trim")
                        .param("password", "validPass123")
                        .param("confirmPassword", "validPass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        // Email duoc cat khoang trang va chuyen chu thuong
        assertThat(userRepository.findByEmail("trimmed.user@example.com")).isPresent();
    }

    @Test
    @DisplayName("(12) Đăng nhập thật qua POST /login, GET / hiện họ tên và không chứa chuỗi hash; model không chứa password")
    void test12_realLoginDisplaysFullNameAndNoPasswordHash() throws Exception {
        User user = new User();
        user.setEmail("student12@example.com");
        user.setPasswordHash(passwordEncoder.encode("secretPass123"));
        user.setFullName("Nguyen Van Muoi Hai");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        var loginResult = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "student12@example.com")
                        .param("password", "secretPass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nguyen Van Muoi Hai")))
                .andExpect(content().string(not(containsString("$2"))))
                .andExpect(model().attributeDoesNotExist("password"))
                .andExpect(model().attributeDoesNotExist("passwordHash"));
    }

    @Test
    @DisplayName("(13) Menu theo role: STUDENT không chứa liên kết /admin/users và /manager/*; ADMIN chứa /admin/users")
    void test13_menuByRoleContentCheck() throws Exception {
        User student = new User();
        student.setEmail("student13@example.com");
        student.setPasswordHash(passwordEncoder.encode("studentPass123"));
        student.setFullName("Sinh Vien Muoi Ba");
        student.setRole(Role.STUDENT);
        student.setEnabled(true);
        student.setCreatedAt(LocalDateTime.now());
        userRepository.save(student);

        User admin = new User();
        admin.setEmail("admin13@example.com");
        admin.setPasswordHash(passwordEncoder.encode("adminPass123"));
        admin.setFullName("Quan Tri Vien Muoi Ba");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        var studentLogin = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "student13@example.com")
                        .param("password", "studentPass123"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession studentSession = (MockHttpSession) studentLogin.getRequest().getSession(false);

        mockMvc.perform(get("/").session(studentSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/matching")))
                .andExpect(content().string(containsString("/groups")))
                .andExpect(content().string(containsString("/rooms")))
                .andExpect(content().string(containsString("/bookings")))
                .andExpect(content().string(containsString("/chat")))
                .andExpect(content().string(containsString("/notifications")))
                .andExpect(content().string(containsString("/profile")))
                .andExpect(content().string(not(containsString("/admin/users"))))
                .andExpect(content().string(not(containsString("/manager/"))));

        var adminLogin = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin13@example.com")
                        .param("password", "adminPass123"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession adminSession = (MockHttpSession) adminLogin.getRequest().getSession(false);

        mockMvc.perform(get("/").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/admin/users")));
    }

    @Test
    @DisplayName("(14) Nút đăng xuất là form POST có trường _csrf, không có liên kết GET tới /logout")
    void test14_logoutIsPostFormWithCsrfNoGetLink() throws Exception {
        User user = new User();
        user.setEmail("user14@example.com");
        user.setPasswordHash(passwordEncoder.encode("userPass14"));
        user.setFullName("User Muoi Bon");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        var login = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "user14@example.com")
                        .param("password", "userPass14"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        String content = mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andReturn().getResponse().getContentAsString();

        assertThat(content).doesNotContain("href=\"/logout\"");
        assertThat(content).doesNotContain("href='/logout'");
    }
}