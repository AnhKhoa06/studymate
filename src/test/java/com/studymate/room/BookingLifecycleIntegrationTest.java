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
import com.studymate.room.state.InvalidBookingStateException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class BookingLifecycleIntegrationTest {

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
    @DisplayName("Sinh viên không thể hủy đơn của nhóm khác; sau khi hủy thì MANAGER không duyệt được và khung giờ được giải phóng")
    void cancelOwnershipAndInvalidApproveAfterCancel() {
        User leader = createUser("leader.life@studymate.test", Role.STUDENT);
        User outsider = createUser("outsider.life@studymate.test", Role.STUDENT);
        User manager = createUser("manager.life@studymate.test", Role.MANAGER);
        Room room = createRoom("B1-401");

        when(groupLeaderChecker.isGroupLeader(leader.getId(), 501L)).thenReturn(true);
        when(groupLeaderChecker.isGroupLeader(outsider.getId(), 501L)).thenReturn(false);

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        setContext(leader);
        BookingDto created = bookingService.createBooking(leader.getId(), createRequest(room.getId(), 501L, start, end));

        // Nguoi ngoai nhom khong duoc huy
        setContext(outsider);
        assertThatThrownBy(() -> bookingService.cancelBooking(created.id(), outsider.getId()))
                .isInstanceOf(AccessDeniedException.class);

        // Sinh vien khong duoc duyet hoac tu choi
        assertThatThrownBy(() -> bookingService.approveBooking(created.id(), outsider.getId()))
                .isInstanceOf(AccessDeniedException.class);

        // Chu don huy thanh cong
        setContext(leader);
        BookingDto cancelled = bookingService.cancelBooking(created.id(), leader.getId());
        assertThat(cancelled.status()).isEqualTo(BookingStatus.CANCELLED);

        // MANAGER duyet don da huy bi tu choi
        setContext(manager);
        assertThatThrownBy(() -> bookingService.approveBooking(created.id(), manager.getId()))
                .isInstanceOf(InvalidBookingStateException.class);

        // Khung gio da duoc giai phong, co the dat lai
        setContext(leader);
        BookingDto rebooked = bookingService.createBooking(leader.getId(), createRequest(room.getId(), 501L, start, end));
        assertThat(rebooked.status()).isEqualTo(BookingStatus.PENDING);

        // MANAGER duyet -> APPROVED, sau do chu don van huy duoc truoc gio hoc
        setContext(manager);
        BookingDto approved = bookingService.approveBooking(rebooked.id(), manager.getId());
        assertThat(approved.status()).isEqualTo(BookingStatus.APPROVED);
        assertThat(approved.reviewedByUserId()).isEqualTo(manager.getId());

        // MANAGER khong duoc goi cancelBooking
        assertThatThrownBy(() -> bookingService.cancelBooking(rebooked.id(), manager.getId()))
                .isInstanceOf(AccessDeniedException.class);

        setContext(leader);
        BookingDto cancelledAfterApprove = bookingService.cancelBooking(rebooked.id(), leader.getId());
        assertThat(cancelledAfterApprove.status()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("MANAGER từ chối kèm lý do và không thể duyệt lại đơn đã từ chối")
    void rejectAndPreventReapproval() {
        User leader = createUser("leader.rej@studymate.test", Role.STUDENT);
        User manager = createUser("manager.rej@studymate.test", Role.MANAGER);
        Room room = createRoom("B1-402");

        when(groupLeaderChecker.isGroupLeader(leader.getId(), 502L)).thenReturn(true);

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        setContext(leader);
        BookingDto created = bookingService.createBooking(leader.getId(), createRequest(room.getId(), 502L, start, end));

        setContext(manager);
        BookingDto rejected = bookingService.rejectBooking(created.id(), manager.getId(), "Phòng dùng cho thi cuối kỳ");
        assertThat(rejected.status()).isEqualTo(BookingStatus.REJECTED);
        assertThat(rejected.rejectionReason()).isEqualTo("Phòng dùng cho thi cuối kỳ");

        assertThatThrownBy(() -> bookingService.approveBooking(created.id(), manager.getId()))
                .isInstanceOf(InvalidBookingStateException.class);
    }

    private void setContext(User user) {
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

    private BookingCreateRequest createRequest(Long roomId, Long groupId, LocalDateTime start, LocalDateTime end) {
        BookingCreateRequest req = new BookingCreateRequest();
        req.setRoomId(roomId);
        req.setGroupId(groupId);
        req.setStartTime(start);
        req.setEndTime(end);
        req.setPurpose("Học nhóm OOP");
        return req;
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

    private Room createRoom(String code) {
        LocalDateTime now = LocalDateTime.now();
        Room r = new Room();
        r.setCode(code);
        r.setBuilding("B1");
        r.setCapacity(15);
        r.setEquipment("Bảng trắng");
        r.setStatus(RoomStatus.AVAILABLE);
        r.setCreatedAt(now);
        r.setUpdatedAt(now);
        return roomRepository.save(r);
    }
}
