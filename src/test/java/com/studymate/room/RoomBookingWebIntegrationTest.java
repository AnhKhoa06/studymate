package com.studymate.room;

import com.studymate.common.security.CurrentUser;
import com.studymate.room.entity.Booking;
import com.studymate.room.entity.BookingStatus;
import com.studymate.room.entity.Room;
import com.studymate.room.entity.RoomStatus;
import com.studymate.room.event.BookingApprovedEvent;
import com.studymate.room.event.BookingCancelledEvent;
import com.studymate.room.event.BookingRejectedEvent;
import com.studymate.room.repository.BookingRepository;
import com.studymate.room.repository.RoomRepository;
import com.studymate.room.service.GroupLeaderChecker;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
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
@RecordApplicationEvents
@ActiveProfiles("test")
class RoomBookingWebIntegrationTest {

    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEvents applicationEvents;

    @MockitoBean
    private GroupLeaderChecker groupLeaderChecker;

    @AfterEach
    void tearDown() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Chưa đăng nhập truy cập /rooms, /bookings, /manager/bookings đều chuyển hướng /login")
    void unauthenticatedRequests_redirectToLogin() throws Exception {
        mockMvc.perform(get("/rooms"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/bookings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/manager/bookings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Phân quyền chéo: STUDENT bị 403 ở /manager/bookings, MANAGER bị 403 ở /bookings, thiếu CSRF bị 403")
    void roleAccessAndCsrfEnforcement() throws Exception {
        User student = createUser("student.acl.web@studymate.test", Role.STUDENT);
        User manager = createUser("manager.acl.web@studymate.test", Role.MANAGER);
        Room room = createRoom("B1-501", RoomStatus.AVAILABLE);

        // STUDENT khong vao duoc /manager/bookings
        mockMvc.perform(get("/manager/bookings").with(user(toPrincipal(student))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/manager/bookings/1/approve")
                        .with(user(toPrincipal(student)))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        // MANAGER khong vao duoc /bookings
        mockMvc.perform(get("/bookings").with(user(toPrincipal(manager))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/bookings")
                        .with(user(toPrincipal(manager)))
                        .with(csrf())
                        .param("roomId", room.getId().toString())
                        .param("groupId", "1")
                        .param("startTime", LocalDateTime.now().plusDays(1).format(ISO_DATE_TIME))
                        .param("endTime", LocalDateTime.now().plusDays(1).plusHours(2).format(ISO_DATE_TIME)))
                .andExpect(status().isForbidden());

        // STUDENT thieu CSRF bi 403
        mockMvc.perform(post("/bookings")
                        .with(user(toPrincipal(student)))
                        .param("roomId", room.getId().toString())
                        .param("groupId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Xem lịch trống theo ngày hiển thị phòng AVAILABLE và khung giờ đã đặt trong ngày")
    void dailySchedule_displaysAvailableRoomsAndBookedSlots() throws Exception {
        User student = createUser("student.sched@studymate.test", Role.STUDENT);
        Room availableRoom = createRoom("B1-502", RoomStatus.AVAILABLE);
        createRoom("B1-503", RoomStatus.MAINTENANCE);

        LocalDate targetDate = LocalDate.now().plusDays(2);
        LocalDateTime slotStart = targetDate.atTime(9, 0);
        LocalDateTime slotEnd = targetDate.atTime(11, 0);
        createBooking(availableRoom, 701L, student.getId(), slotStart, slotEnd, BookingStatus.APPROVED);

        mockMvc.perform(get("/rooms")
                        .param("date", targetDate.toString())
                        .with(user(toPrincipal(student))))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("roomsPage", "schedulesByRoomId"))
                .andExpect(content().string(containsString("B1-502")))
                .andExpect(content().string(containsString("09:00 - 11:00")))
                .andExpect(content().string(not(containsString("B1-503"))));
    }

    @Test
    @DisplayName("STUDENT đặt phòng, bị chặn nếu không phải trưởng nhóm hoặc trùng giờ, người ngoài bị 403 khi hủy")
    void studentBookingCreateOverlapAndCancelOwnership() throws Exception {
        User leader = createUser("leader.web@studymate.test", Role.STUDENT);
        User outsider = createUser("outsider.web@studymate.test", Role.STUDENT);
        Room room = createRoom("B1-504", RoomStatus.AVAILABLE);

        when(groupLeaderChecker.isGroupLeader(leader.getId(), 801L)).thenReturn(true);
        when(groupLeaderChecker.isGroupLeader(outsider.getId(), 801L)).thenReturn(false);

        LocalDateTime start = LocalDateTime.now().plusDays(3).withHour(8).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        // Khong phai truong nhom dat phong -> 403
        mockMvc.perform(post("/bookings")
                        .with(user(toPrincipal(outsider)))
                        .with(csrf())
                        .param("roomId", room.getId().toString())
                        .param("groupId", "801")
                        .param("startTime", start.format(ISO_DATE_TIME))
                        .param("endTime", end.format(ISO_DATE_TIME))
                        .param("purpose", "Học nhóm"))
                .andExpect(status().isForbidden());

        // Truong nhom dat phong thanh cong
        mockMvc.perform(post("/bookings")
                        .with(user(toPrincipal(leader)))
                        .with(csrf())
                        .param("roomId", room.getId().toString())
                        .param("groupId", "801")
                        .param("startTime", start.format(ISO_DATE_TIME))
                        .param("endTime", end.format(ISO_DATE_TIME))
                        .param("purpose", "Ôn tập OOP"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attributeExists("successMessage"));

        Booking saved = bookingRepository.findAll().get(0);
        assertThat(saved.getStatus()).isEqualTo(BookingStatus.PENDING);

        // Dat trung gio -> bao loi tren form, khong tao ban ghi thu 2
        mockMvc.perform(post("/bookings")
                        .with(user(toPrincipal(leader)))
                        .with(csrf())
                        .param("roomId", room.getId().toString())
                        .param("groupId", "801")
                        .param("startTime", start.plusHours(1).format(ISO_DATE_TIME))
                        .param("endTime", end.plusHours(1).format(ISO_DATE_TIME))
                        .param("purpose", "Đặt trùng"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());

        assertThat(bookingRepository.count()).isEqualTo(1);

        // Nguoi ngoai huy don -> 403
        mockMvc.perform(post("/bookings/" + saved.getId() + "/cancel")
                        .with(user(toPrincipal(outsider)))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertThat(bookingRepository.findById(saved.getId()).orElseThrow().getStatus())
                .isEqualTo(BookingStatus.PENDING);

        // Chu don huy thanh cong va phat BookingCancelledEvent
        mockMvc.perform(post("/bookings/" + saved.getId() + "/cancel")
                        .with(user(toPrincipal(leader)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"))
                .andExpect(flash().attributeExists("successMessage"));

        assertThat(bookingRepository.findById(saved.getId()).orElseThrow().getStatus())
                .isEqualTo(BookingStatus.CANCELLED);
        assertThat(applicationEvents.stream(BookingCancelledEvent.class).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("MANAGER duyệt/từ chối phát event tương ứng và từ chối chuyển trạng thái sai")
    void managerApproveAndRejectPublishEventsAndRejectInvalidTransition() throws Exception {
        User student = createUser("student.mgr.web@studymate.test", Role.STUDENT);
        User manager = createUser("manager.web@studymate.test", Role.MANAGER);
        Room room = createRoom("B1-505", RoomStatus.AVAILABLE);

        LocalDateTime day = LocalDateTime.now().plusDays(4).withHour(8).withMinute(0).withSecond(0).withNano(0);
        Booking pending1 = createBooking(room, 901L, student.getId(), day, day.plusHours(2), BookingStatus.PENDING);
        Booking pending2 = createBooking(room, 902L, student.getId(), day.plusHours(3), day.plusHours(5), BookingStatus.PENDING);
        Booking cancelled = createBooking(room, 903L, student.getId(), day.plusHours(6), day.plusHours(8), BookingStatus.CANCELLED);

        mockMvc.perform(get("/manager/bookings").with(user(toPrincipal(manager))))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("bookingsPage"))
                .andExpect(content().string(containsString("B1-505")));

        // Duyet -> APPROVED + phat BookingApprovedEvent
        mockMvc.perform(post("/manager/bookings/" + pending1.getId() + "/approve")
                        .with(user(toPrincipal(manager)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/bookings"))
                .andExpect(flash().attributeExists("successMessage"));

        assertThat(bookingRepository.findById(pending1.getId()).orElseThrow().getStatus())
                .isEqualTo(BookingStatus.APPROVED);
        assertThat(applicationEvents.stream(BookingApprovedEvent.class).toList())
                .singleElement()
                .satisfies(ev -> {
                    assertThat(ev.bookingId()).isEqualTo(pending1.getId());
                    assertThat(ev.approvedByUserId()).isEqualTo(manager.getId());
                });

        // Tu choi -> REJECTED + phat BookingRejectedEvent
        mockMvc.perform(post("/manager/bookings/" + pending2.getId() + "/reject")
                        .with(user(toPrincipal(manager)))
                        .with(csrf())
                        .param("reason", "Trùng lịch bảo trì"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/bookings"))
                .andExpect(flash().attributeExists("successMessage"));

        Booking reloaded2 = bookingRepository.findById(pending2.getId()).orElseThrow();
        assertThat(reloaded2.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(reloaded2.getRejectionReason()).isEqualTo("Trùng lịch bảo trì");
        assertThat(applicationEvents.stream(BookingRejectedEvent.class).toList())
                .singleElement()
                .satisfies(ev -> assertThat(ev.reason()).isEqualTo("Trùng lịch bảo trì"));

        // Duyet don da huy -> thong bao loi, trang thai van la CANCELLED
        mockMvc.perform(post("/manager/bookings/" + cancelled.getId() + "/approve")
                        .with(user(toPrincipal(manager)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/bookings"))
                .andExpect(flash().attributeExists("errorMessage"));

        assertThat(bookingRepository.findById(cancelled.getId()).orElseThrow().getStatus())
                .isEqualTo(BookingStatus.CANCELLED);
    }

    private User createUser(String email, Role role) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode("Secret123!"));
        u.setFullName("Test User");
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

    private Room createRoom(String code, RoomStatus status) {
        LocalDateTime now = LocalDateTime.now();
        Room r = new Room();
        r.setCode(code);
        r.setBuilding("B1");
        r.setCapacity(12);
        r.setEquipment("Máy chiếu");
        r.setStatus(status);
        r.setCreatedAt(now);
        r.setUpdatedAt(now);
        return roomRepository.save(r);
    }

    private Booking createBooking(Room room,
                                  Long groupId,
                                  Long userId,
                                  LocalDateTime start,
                                  LocalDateTime end,
                                  BookingStatus status) {
        LocalDateTime now = LocalDateTime.now();
        Booking b = new Booking();
        b.setRoom(room);
        b.setGroupId(groupId);
        b.setRequestedByUserId(userId);
        b.setStartTime(start);
        b.setEndTime(end);
        b.setPurpose("Ôn tập");
        b.setStatus(status);
        b.setCreatedAt(now);
        b.setUpdatedAt(now);
        return bookingRepository.save(b);
    }
}
