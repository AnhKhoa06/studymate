package com.studymate.room;

import com.studymate.common.security.CurrentUser;
import com.studymate.room.entity.Room;
import com.studymate.room.entity.RoomStatus;
import com.studymate.room.repository.RoomRepository;
import com.studymate.user.entity.Role;
import com.studymate.user.entity.User;
import com.studymate.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ManagerRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        roomRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Chưa đăng nhập vào /manager/rooms bị chuyển hướng tới /login")
    void managerRooms_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/manager/rooms"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("STUDENT và ADMIN truy cập /manager/rooms bị từ chối 403")
    void managerRooms_nonManagerRole_returns403() throws Exception {
        User student = createUser("student.room@studymate.test", "Sinh Vien", Role.STUDENT);
        User admin = createUser("admin.room@studymate.test", "Quan Tri", Role.ADMIN);

        mockMvc.perform(get("/manager/rooms").with(user(toPrincipal(student))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/manager/rooms/new").with(user(toPrincipal(student))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/manager/rooms")
                        .with(user(toPrincipal(student)))
                        .with(csrf())
                        .param("code", "B1-101")
                        .param("building", "B1")
                        .param("capacity", "10")
                        .param("status", "AVAILABLE"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/manager/rooms").with(user(toPrincipal(admin))))
                .andExpect(status().isForbidden());

        assertThat(roomRepository.count()).isZero();
    }

    @Test
    @DisplayName("MANAGER gửi POST /manager/rooms thiếu CSRF bị chặn 403")
    void managerRooms_postWithoutCsrf_returns403() throws Exception {
        User manager = createUser("manager.csrf@studymate.test", "Quan Ly", Role.MANAGER);

        mockMvc.perform(post("/manager/rooms")
                        .with(user(toPrincipal(manager)))
                        .param("code", "B1-101")
                        .param("building", "B1")
                        .param("capacity", "10")
                        .param("status", "AVAILABLE"))
                .andExpect(status().isForbidden());

        assertThat(roomRepository.count()).isZero();
    }

    @Test
    @DisplayName("MANAGER tìm kiếm, phân trang, tạo, sửa, đổi trạng thái phòng và bị chặn khi trùng mã")
    void managerRooms_crudSearchAndDuplicateValidation() throws Exception {
        User manager = createUser("manager.ok@studymate.test", "Quan Ly", Role.MANAGER);
        CurrentUser managerPrincipal = toPrincipal(manager);

        createRoom("A1-101", "A1", 8, "Bảng trắng", RoomStatus.AVAILABLE);

        // Tim kiem va phan trang
        mockMvc.perform(get("/manager/rooms")
                        .param("q", "A1")
                        .param("status", "AVAILABLE")
                        .with(user(managerPrincipal)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("roomsPage"))
                .andExpect(content().string(containsString("A1-101")));

        // Tao phong moi (chuan hoa chu hoa ma phong)
        mockMvc.perform(post("/manager/rooms")
                        .with(user(managerPrincipal))
                        .with(csrf())
                        .param("code", " b1-202 ")
                        .param("building", "B1")
                        .param("capacity", "16")
                        .param("equipment", "Máy chiếu")
                        .param("status", "AVAILABLE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/rooms"))
                .andExpect(flash().attributeExists("successMessage"));

        assertThat(roomRepository.existsByCode("B1-202")).isTrue();

        // Trung ma phong -> hien loi o truong code
        mockMvc.perform(post("/manager/rooms")
                        .with(user(managerPrincipal))
                        .with(csrf())
                        .param("code", "B1-202")
                        .param("building", "B1")
                        .param("capacity", "12")
                        .param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("roomForm", "code"));

        assertThat(roomRepository.count()).isEqualTo(2);

        // Cap nhat phong va doi trang thai
        Room created = roomRepository.findAll().stream()
                .filter(r -> "B1-202".equals(r.getCode()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/manager/rooms/" + created.getId())
                        .with(user(managerPrincipal))
                        .with(csrf())
                        .param("code", "B1-202")
                        .param("building", "B1")
                        .param("capacity", "20")
                        .param("equipment", "Máy chiếu, Bảng trắng")
                        .param("status", "AVAILABLE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/rooms"));

        mockMvc.perform(post("/manager/rooms/" + created.getId() + "/status")
                        .with(user(managerPrincipal))
                        .with(csrf())
                        .param("newStatus", "MAINTENANCE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/rooms"))
                .andExpect(flash().attributeExists("successMessage"));

        Room reloaded = roomRepository.findById(created.getId()).orElseThrow();
        assertThat(reloaded.getCapacity()).isEqualTo(20);
        assertThat(reloaded.getStatus()).isEqualTo(RoomStatus.MAINTENANCE);
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

    private CurrentUser toPrincipal(User user) {
        return new CurrentUser(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getRole(),
                user.isEnabled(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    private Room createRoom(String code, String building, int capacity, String equipment, RoomStatus status) {
        LocalDateTime now = LocalDateTime.now();
        Room room = new Room();
        room.setCode(code);
        room.setBuilding(building);
        room.setCapacity(capacity);
        room.setEquipment(equipment);
        room.setStatus(status);
        room.setCreatedAt(now);
        room.setUpdatedAt(now);
        return roomRepository.save(room);
    }
}
