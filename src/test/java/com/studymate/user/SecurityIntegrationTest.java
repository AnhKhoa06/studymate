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
import java.util.regex.Pattern;

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

        String studentContent = mockMvc.perform(get("/").session(studentSession))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(studentContent).contains("/matching");
        assertThat(studentContent).contains("/groups");
        assertThat(studentContent).doesNotContain("href=\"/rooms\"");
        assertThat(studentContent).contains("/bookings");
        assertThat(studentContent).contains("/chat");
        assertThat(studentContent).contains("/notifications");
        assertThat(studentContent).contains("/profile");
        assertThat(studentContent).doesNotContain("/admin/users");
        assertThat(studentContent).doesNotContain("/manager/");

        var studentNavMatcher = Pattern.compile("<nav\\s[^>]*class=\"[^\"]*\\bnavbar__nav\\b[^\"]*\"[^>]*>.*?</nav>", Pattern.DOTALL).matcher(studentContent);
        assertThat(studentNavMatcher.find()).isTrue();
        String studentNav = studentNavMatcher.group();
        assertThat(studentNav).doesNotContain("href=\"/rooms\"");
        assertThat(studentNav).doesNotContain("/admin/users");
        assertThat(studentNav).doesNotContain("/manager/");

        var adminLogin = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin13@example.com")
                        .param("password", "adminPass123"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession adminSession = (MockHttpSession) adminLogin.getRequest().getSession(false);

        String adminContent = mockMvc.perform(get("/").session(adminSession))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var adminSidebarMatcher = Pattern.compile("<aside\\s[^>]*class=\"[^\"]*\\bsidebar\\b[^\"]*\"[^>]*>.*?</aside>", Pattern.DOTALL).matcher(adminContent);
        assertThat(adminSidebarMatcher.find()).isTrue();
        assertThat(adminSidebarMatcher.group()).contains("href=\"/admin/users\"");
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

    @Test
    @DisplayName("(15) Trang /login có thẻ img logo, có nút hiện/ẩn mật khẩu với aria-label, và ô password không có thuộc tính value sau khi đăng ký lỗi")
    void test15_loginElementsAndRegisterPasswordNoValueOnError() throws Exception {
        // Kiem tra trang /login co img logo va nut toggle password co aria-label
        String loginContent = mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<img")))
                .andExpect(content().string(containsString("logo.png")))
                .andExpect(content().string(containsString("aria-label=\"Hiện mật khẩu\"")))
                .andReturn().getResponse().getContentAsString();
        assertThat(loginContent).contains("alt=\"Logo Trường Đại học Quy Nhơn\"");

        // Dang ky loi (khong khop mat khau): o password khong duoc chua thuoc tinh value
        String registerErrorContent = mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "errorpass@example.com")
                        .param("fullName", "Error User")
                        .param("password", "validPass123")
                        .param("confirmPassword", "differentPass123"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(registerErrorContent).doesNotContainPattern("id=\"password\"[^>]*value=");
        assertThat(registerErrorContent).doesNotContainPattern("id=\"confirmPassword\"[^>]*value=");
    }

    @Test
    @DisplayName("(16) MANAGER và ADMIN đăng nhập thật rồi vào / thấy khung staff và không thấy thanh tab dưới của sinh viên")
    void test16_managerAndAdminRealLoginAccessHomeSeeStaffLayoutNoBottomNav() throws Exception {
        // MANAGER dang nhap that
        User manager = new User();
        manager.setEmail("manager16@example.com");
        manager.setPasswordHash(passwordEncoder.encode("managerPass16"));
        manager.setFullName("Quan Ly Muoi Sau");
        manager.setRole(Role.MANAGER);
        manager.setEnabled(true);
        manager.setCreatedAt(LocalDateTime.now());
        userRepository.save(manager);

        var managerLogin = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "manager16@example.com")
                        .param("password", "managerPass16"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession managerSession = (MockHttpSession) managerLogin.getRequest().getSession(false);
        assertThat(managerSession).isNotNull();

        String managerContent = mockMvc.perform(get("/").session(managerSession))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(managerContent).containsPattern("<body\\s[^>]*class=\"[^\"]*\\blayout-staff\\b[^\"]*\"[^>]*>");
        assertThat(managerContent).doesNotContainPattern("\\bcontainer-student\\b");
        assertThat(managerContent).doesNotContainPattern("\\bnav-bottom\\b");
        assertThat(managerContent).doesNotContain("nav-bottom__item");

        // <aside nam TRUOC the <main trong HTML
        var mgrAsideMatcher = Pattern.compile("<aside\\b[^>]*>", Pattern.CASE_INSENSITIVE).matcher(managerContent);
        assertThat(mgrAsideMatcher.find()).isTrue();
        int mgrAsideIndex = mgrAsideMatcher.start();

        var mgrMainMatcher = Pattern.compile("<main\\b[^>]*>", Pattern.CASE_INSENSITIVE).matcher(managerContent);
        assertThat(mgrMainMatcher.find()).isTrue();
        int mgrMainIndex = mgrMainMatcher.start();

        assertThat(mgrAsideIndex).isLessThan(mgrMainIndex);

        // Sidebar manager chua cac muc chuc nang
        var mgrSidebarMatcher = Pattern.compile("<aside\\s[^>]*class=\"[^\"]*\\bsidebar\\b[^\"]*\"[^>]*>.*?</aside>", Pattern.DOTALL).matcher(managerContent);
        assertThat(mgrSidebarMatcher.find()).isTrue();
        String mgrSidebar = mgrSidebarMatcher.group();
        assertThat(mgrSidebar).contains("Quản lý phòng");
        assertThat(mgrSidebar).contains("Duyệt đặt phòng");
        assertThat(mgrSidebar).contains("Thống kê");

        // DUNG MOT the <h1> tren toan trang
        var mgrH1Matcher = Pattern.compile("<h1[\\s>]", Pattern.CASE_INSENSITIVE).matcher(managerContent);
        int mgrH1Count = 0;
        while (mgrH1Matcher.find()) mgrH1Count++;
        assertThat(mgrH1Count).isEqualTo(1);

        // ADMIN dang nhap that
        User admin = new User();
        admin.setEmail("admin16@example.com");
        admin.setPasswordHash(passwordEncoder.encode("adminPass16"));
        admin.setFullName("Admin Muoi Sau");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        var adminLogin = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin16@example.com")
                        .param("password", "adminPass16"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession adminSession = (MockHttpSession) adminLogin.getRequest().getSession(false);
        assertThat(adminSession).isNotNull();

        String adminContent = mockMvc.perform(get("/").session(adminSession))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(adminContent).containsPattern("<body\\s[^>]*class=\"[^\"]*\\blayout-staff\\b[^\"]*\"[^>]*>");
        assertThat(adminContent).doesNotContainPattern("\\bcontainer-student\\b");
        assertThat(adminContent).doesNotContainPattern("\\bnav-bottom\\b");
        assertThat(adminContent).doesNotContain("nav-bottom__item");

        // <aside nam TRUOC the <main trong HTML
        var adminAsideMatcher = Pattern.compile("<aside\\b[^>]*>", Pattern.CASE_INSENSITIVE).matcher(adminContent);
        assertThat(adminAsideMatcher.find()).isTrue();
        int adminAsideIndex = adminAsideMatcher.start();

        var adminMainMatcher = Pattern.compile("<main\\b[^>]*>", Pattern.CASE_INSENSITIVE).matcher(adminContent);
        assertThat(adminMainMatcher.find()).isTrue();
        int adminMainIndex = adminMainMatcher.start();

        assertThat(adminAsideIndex).isLessThan(adminMainIndex);

        // Sidebar admin chua muc Tai khoan
        var adminSidebarMatcher = Pattern.compile("<aside\\s[^>]*class=\"[^\"]*\\bsidebar\\b[^\"]*\"[^>]*>.*?</aside>", Pattern.DOTALL).matcher(adminContent);
        assertThat(adminSidebarMatcher.find()).isTrue();
        String adminSidebar = adminSidebarMatcher.group();
        assertThat(adminSidebar).contains("Tài khoản");

        // DUNG MOT the <h1> tren toan trang
        var adminH1Matcher = Pattern.compile("<h1[\\s>]", Pattern.CASE_INSENSITIVE).matcher(adminContent);
        int adminH1Count = 0;
        while (adminH1Matcher.find()) adminH1Count++;
        assertThat(adminH1Count).isEqualTo(1);
    }

    @Test
    @DisplayName("(17) STUDENT đăng nhập: nút theme chỉ dùng icon có aria-label, kiểm tra vị trí /profile trong avatar menu, /notifications ở chuông, hàng menu không chứa hai mục đó, form đăng xuất POST CSRF")
    void test17_studentThemeIconsAndMenuElementLocations() throws Exception {
        User student = new User();
        student.setEmail("student17@example.com");
        student.setPasswordHash(passwordEncoder.encode("studentPass17"));
        student.setFullName("Sinh Vien Muoi Bay");
        student.setRole(Role.STUDENT);
        student.setEnabled(true);
        student.setCreatedAt(LocalDateTime.now());
        userRepository.save(student);

        var login = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "student17@example.com")
                        .param("password", "studentPass17"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();

        String content = mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 1. Nut theme co aria-label, khong dung chu "Sang", "Toi", "He thong"
        assertThat(content).contains("aria-label=\"Giao diện sáng\"");
        assertThat(content).contains("aria-label=\"Giao diện tối\"");
        assertThat(content).contains("aria-label=\"Theo hệ thống\"");
        assertThat(content).doesNotContain(">Sáng<");
        assertThat(content).doesNotContain(">Tối<");
        assertThat(content).doesNotContain(">Hệ thống<");

        // 2. Kiem tra vi tri: hang menu desktop <nav class="navbar__nav">...</nav> KHONG chua "Thong bao" va "Ho so", khong chua /rooms, khong icon
        var navMatcher = Pattern.compile("<nav\\s[^>]*class=\"[^\"]*\\bnavbar__nav\\b[^\"]*\"[^>]*>.*?</nav>", Pattern.DOTALL).matcher(content);
        assertThat(navMatcher.find()).isTrue();
        String desktopMenuContent = navMatcher.group();
        assertThat(desktopMenuContent).doesNotContain("/notifications");
        assertThat(desktopMenuContent).doesNotContain("/profile");
        assertThat(desktopMenuContent).doesNotContain("Thông báo");
        assertThat(desktopMenuContent).doesNotContain("Hồ sơ");
        assertThat(desktopMenuContent).doesNotContain("/rooms");
        assertThat(desktopMenuContent).doesNotContain("<svg");
        assertThat(desktopMenuContent).contains("href=\"/\"");
        assertThat(desktopMenuContent).contains("href=\"/matching\"");
        assertThat(desktopMenuContent).contains("href=\"/groups\"");
        assertThat(desktopMenuContent).contains("href=\"/bookings\"");
        assertThat(desktopMenuContent).contains("href=\"/chat\"");

        // Hang menu sinh vien co dung 5 lien ket (Trang chu, Tim ban, Nhom, Dat phong, Chat)
        var linkMatcher = Pattern.compile("<a\\s[^>]*href=\"[^\"]*\"[^>]*>", Pattern.DOTALL).matcher(desktopMenuContent);
        int linkCount = 0;
        while (linkMatcher.find()) linkCount++;
        assertThat(linkCount).isEqualTo(5);

        // 3. /notifications nam o nut chuong (trich ca the mo chua navbar__bell-btn)
        var bellMatcher = Pattern.compile("<a\\s[^>]*class=\"[^\"]*\\bnavbar__bell-btn\\b[^\"]*\"[^>]*>", Pattern.DOTALL).matcher(content);
        assertThat(bellMatcher.find()).isTrue();
        String bellTag = bellMatcher.group();
        assertThat(bellTag).contains("href=\"/notifications\"");
        assertThat(bellTag).contains("aria-label=\"Thông báo\"");

        // 4. /profile nam trong menu avatar (userMenuDropdown) trich bang regex
        var headerMatcher = Pattern.compile("<header\\s[^>]*class=\"[^\"]*\\bnavbar\\b[^\"]*\"[^>]*>.*?</header>", Pattern.DOTALL).matcher(content);
        assertThat(headerMatcher.find()).isTrue();
        String headerContent = headerMatcher.group();

        var dropdownMatcher = Pattern.compile("<div\\s[^>]*id=\"userMenuDropdown\"[^>]*>.*?</form>\\s*</div>", Pattern.DOTALL).matcher(headerContent);
        assertThat(dropdownMatcher.find()).isTrue();
        String userMenuContent = dropdownMatcher.group();

        var profileLinkMatcher = Pattern.compile("<a\\s[^>]*href=\"/profile\"[^>]*>.*?</a>", Pattern.DOTALL).matcher(userMenuContent);
        assertThat(profileLinkMatcher.find()).isTrue();
        assertThat(profileLinkMatcher.group()).contains("Hồ sơ");

        // 5. Cum theme nam trong menu avatar va ca o khu vuc tac vu ben ngoai dropdown
        assertThat(userMenuContent).contains("class=\"theme-switcher\"");
        assertThat(userMenuContent).contains("Giao diện");

        String outsideDropdown = headerContent.replace(userMenuContent, "");
        assertThat(outsideDropdown).contains("class=\"theme-switcher\"");

        // 6. Form dang xuat la POST co _csrf nam trong menu avatar
        var logoutFormMatcher = Pattern.compile("<form\\s[^>]*action=\"/logout\"[^>]*method=\"post\"[^>]*>.*?</form>", Pattern.DOTALL).matcher(userMenuContent);
        assertThat(logoutFormMatcher.find()).isTrue();
        assertThat(logoutFormMatcher.group()).contains("name=\"_csrf\"");

        // 7. Trang chu co home-hero, dung 4 the journey-card co chua <svg, khong chua journey-card__number, khong chua chuoi cu
        assertThat(content).contains("home-hero");
        assertThat(content).doesNotContain("journey-card__number");
        assertThat(content).doesNotContain("Phát triển phần mềm hướng đối tượng");

        var journeyCardMatcher = Pattern.compile("<a\\s[^>]*class=\"[^\"]*\\bjourney-card\\b[^\"]*\"[^>]*>.*?</a>", Pattern.DOTALL).matcher(content);
        int cardCount = 0;
        while (journeyCardMatcher.find()) {
            cardCount++;
            String cardHtml = journeyCardMatcher.group();
            assertThat(cardHtml).contains("<svg");
            assertThat(cardHtml).doesNotContain("journey-card__number");
        }
        assertThat(cardCount).isEqualTo(4);

        // 8. Thanh rail chua vach truot indicator va trang co nap navbar.js
        var railMatcher = Pattern.compile("<div\\s[^>]*class=\"[^\"]*\\bnavbar__rail\\b[^\"]*\"[^>]*>.*?</div>", Pattern.DOTALL).matcher(content);
        assertThat(railMatcher.find()).isTrue();
        String railContent = railMatcher.group();
        assertThat(railContent).contains("<span class=\"navbar__indicator\" aria-hidden=\"true\"></span>");
        assertThat(content).contains("navbar.js");
    }

    @Test
    @DisplayName("(18) GET /login khi chua co phien tra 200 va trang chua _csrf")
    void test18_loginPageReturns200WithCsrf() throws Exception {
        // LUU Y: MockMvc khong mo phong duoc viec Tomcat gui response tung phan (flush),
        // nen test nay khong phat hien duoc loi IllegalStateException tren Tomcat thuc.
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("_csrf")));
    }

    @Test
    @DisplayName("(19) GET /register khi chua co phien tra 200 va trang chua _csrf")
    void test19_registerPageReturns200WithCsrf() throws Exception {
        // LUU Y: Nhu test 18, MockMvc khong mo phong duoc flush tung phan cua Tomcat.
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("_csrf")));
    }

    @Test
    @DisplayName("(20) GET /login trả 200, có nhãn 'Email', input name='username', và KHÔNG chứa chuỗi 'Tên đăng nhập'")
    void test20_loginPageLabelEmailAndNoUsernameText() throws Exception {
        String loginContent = mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Trich the label for="username" co class form-field__label
        var labelMatcher = Pattern.compile("<label\\s[^>]*for=\"username\"[^>]*>.*?</label>", Pattern.DOTALL).matcher(loginContent);
        assertThat(labelMatcher.find()).isTrue();
        assertThat(labelMatcher.group()).contains("form-field__label");
        assertThat(labelMatcher.group()).contains("Email");

        // Trich the input name="username"
        var inputMatcher = Pattern.compile("<input\\s[^>]*name=\"username\"[^>]*>", Pattern.DOTALL).matcher(loginContent);
        assertThat(inputMatcher.find()).isTrue();
        String inputTag = inputMatcher.group();
        assertThat(inputTag).contains("type=\"email\"");
        assertThat(inputTag).contains("autocomplete=\"username\"");

        assertThat(loginContent).doesNotContain("Tên đăng nhập");
    }
}