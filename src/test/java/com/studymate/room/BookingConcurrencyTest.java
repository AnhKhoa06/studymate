package com.studymate.room;

import com.studymate.common.security.CurrentUser;
import com.studymate.room.dto.BookingCreateRequest;
import com.studymate.room.dto.BookingDto;
import com.studymate.room.entity.BookingStatus;
import com.studymate.room.entity.Room;
import com.studymate.room.entity.RoomStatus;
import com.studymate.room.repository.BookingRepository;
import com.studymate.room.repository.RoomRepository;
import com.studymate.room.service.BookingService;
import com.studymate.room.service.GroupLeaderChecker;
import com.studymate.user.entity.Role;
import com.studymate.user.entity.User;
import com.studymate.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private GroupLeaderChecker groupLeaderChecker;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Hai yêu cầu đặt trùng khung giờ đến đồng thời: chỉ đúng 1 yêu cầu thành công")
    void concurrentBookings_sameRoomOverlappingSlot_onlyOneSucceeds() throws Exception {
        User leader1 = createStudent("leader1.concurrent@studymate.test", "Truong Nhom 1");
        User leader2 = createStudent("leader2.concurrent@studymate.test", "Truong Nhom 2");
        Room room = createRoom("B1-301", RoomStatus.AVAILABLE);

        when(groupLeaderChecker.isGroupLeader(leader1.getId(), 101L)).thenReturn(true);
        when(groupLeaderChecker.isGroupLeader(leader2.getId(), 102L)).thenReturn(true);

        LocalDateTime start1 = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end1 = start1.plusHours(2);
        // Chong lan 1 gio voi yeu cau 1
        LocalDateTime start2 = start1.plusHours(1);
        LocalDateTime end2 = start2.plusHours(2);

        BookingCreateRequest req1 = buildRequest(room.getId(), 101L, start1, end1, "Nhóm 1 học OOP");
        BookingCreateRequest req2 = buildRequest(room.getId(), 102L, start2, end2, "Nhóm 2 học CSDL");

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Outcome> f1 = executor.submit(createBookingTask(leader1, req1, readyLatch, startLatch));
            Future<Outcome> f2 = executor.submit(createBookingTask(leader2, req2, readyLatch, startLatch));

            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();

            Outcome r1 = f1.get(15, TimeUnit.SECONDS);
            Outcome r2 = f2.get(15, TimeUnit.SECONDS);

            List<Outcome> outcomes = List.of(r1, r2);
            long successCount = outcomes.stream().filter(Outcome::succeeded).count();
            long conflictCount = outcomes.stream().filter(Outcome::conflicted).count();

            assertThat(successCount).isEqualTo(1);
            assertThat(conflictCount).isEqualTo(1);
            assertThat(bookingRepository.count()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Thành viên thường (không phải trưởng nhóm) hoặc phòng bảo trì không thể đặt phòng")
    void createBooking_rejectsNonLeaderAndMaintenanceRoom() {
        User member = createStudent("member.book@studymate.test", "Thanh Vien");
        Room availableRoom = createRoom("B1-302", RoomStatus.AVAILABLE);
        Room maintenanceRoom = createRoom("B1-303", RoomStatus.MAINTENANCE);

        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        setStudentContext(member);

        // Khong phai truong nhom
        when(groupLeaderChecker.isGroupLeader(member.getId(), 201L)).thenReturn(false);
        BookingCreateRequest nonLeaderReq = buildRequest(availableRoom.getId(), 201L, start, end, "Học nhóm");
        assertThatThrownBy(() -> bookingService.createBooking(member.getId(), nonLeaderReq))
                .isInstanceOf(AccessDeniedException.class);

        // La truong nhom nhung phong dang bao tri
        when(groupLeaderChecker.isGroupLeader(member.getId(), 202L)).thenReturn(true);
        BookingCreateRequest maintenanceReq = buildRequest(maintenanceRoom.getId(), 202L, start, end, "Học nhóm");
        assertThatThrownBy(() -> bookingService.createBooking(member.getId(), maintenanceReq))
                .isInstanceOf(BookingService.RoomUnavailableException.class);

        // Dat phong san sang -> thanh cong, dat noi tiep sat gio -> thanh cong
        BookingDto first = bookingService.createBooking(
                member.getId(),
                buildRequest(availableRoom.getId(), 202L, start, end, "Ca 1")
        );
        BookingDto adjacent = bookingService.createBooking(
                member.getId(),
                buildRequest(availableRoom.getId(), 202L, end, end.plusHours(2), "Ca 2")
        );

        assertThat(first.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(adjacent.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(bookingRepository.count()).isEqualTo(2);
    }

    private Callable<Outcome> createBookingTask(User student,
                                                BookingCreateRequest request,
                                                CountDownLatch readyLatch,
                                                CountDownLatch startLatch) {
        return () -> {
            setStudentContext(student);
            readyLatch.countDown();
            if (!startLatch.await(5, TimeUnit.SECONDS)) {
                return Outcome.error();
            }
            try {
                bookingService.createBooking(student.getId(), request);
                return Outcome.success();
            } catch (BookingService.BookingConflictException ex) {
                return Outcome.conflict();
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }

    private void setStudentContext(User user) {
        CurrentUser principal = new CurrentUser(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getRole(),
                user.isEnabled(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        var auth = UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    private BookingCreateRequest buildRequest(Long roomId,
                                              Long groupId,
                                              LocalDateTime start,
                                              LocalDateTime end,
                                              String purpose) {
        BookingCreateRequest req = new BookingCreateRequest();
        req.setRoomId(roomId);
        req.setGroupId(groupId);
        req.setStartTime(start);
        req.setEndTime(end);
        req.setPurpose(purpose);
        return req;
    }

    private User createStudent(String email, String fullName) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode("Secret123!"));
        u.setFullName(fullName);
        u.setRole(Role.STUDENT);
        u.setEnabled(true);
        u.setCreatedAt(LocalDateTime.now());
        return userRepository.save(u);
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

    private record Outcome(boolean succeeded, boolean conflicted) {
        static Outcome success() {
            return new Outcome(true, false);
        }

        static Outcome conflict() {
            return new Outcome(false, true);
        }

        static Outcome error() {
            return new Outcome(false, false);
        }
    }
}
