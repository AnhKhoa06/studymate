package com.studymate.room;

import com.studymate.room.entity.Booking;
import com.studymate.room.entity.BookingStatus;
import com.studymate.room.entity.Room;
import com.studymate.room.entity.RoomStatus;
import com.studymate.room.repository.BookingRepository;
import com.studymate.room.repository.RoomRepository;
import com.studymate.user.entity.Role;
import com.studymate.user.entity.User;
import com.studymate.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private Room room;
    private Room otherRoom;
    private User requester;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        room = createRoom("B1-201");
        otherRoom = createRoom("B1-202");
        requester = createRequester("booking.repo@studymate.test");
    }

    @Test
    void existsOverlappingBooking_detectsOverlapsAndAllowsAdjacentOrInactiveSlots() {
        LocalDateTime baseStart = LocalDateTime.of(2026, 12, 10, 8, 0);
        LocalDateTime baseEnd = LocalDateTime.of(2026, 12, 10, 10, 0);

        // Dat khung 08:00 - 10:00 o trang thai PENDING
        createBooking(room, baseStart, baseEnd, BookingStatus.PENDING);

        // Cac truong hop chong lan phai tra ve true
        assertThat(overlaps(room.getId(), baseStart, baseEnd)).isTrue(); // trung khop
        assertThat(overlaps(room.getId(), baseStart.minusHours(1), baseStart.plusHours(1))).isTrue(); // cat dau
        assertThat(overlaps(room.getId(), baseEnd.minusHours(1), baseEnd.plusHours(1))).isTrue(); // cat duoi
        assertThat(overlaps(room.getId(), baseStart.plusMinutes(30), baseEnd.minusMinutes(30))).isTrue(); // nam gon ben trong
        assertThat(overlaps(room.getId(), baseStart.minusHours(1), baseEnd.plusHours(1))).isTrue(); // bao trum

        // Dat noi tiep sat gio (06:00-08:00 va 10:00-12:00) phai cho phep (false)
        assertThat(overlaps(room.getId(), baseStart.minusHours(2), baseStart)).isFalse();
        assertThat(overlaps(room.getId(), baseEnd, baseEnd.plusHours(2))).isFalse();

        // Phong khac cung khung gio phai cho phep (false)
        assertThat(overlaps(otherRoom.getId(), baseStart, baseEnd)).isFalse();

        // Phong co lich CANCELLED/REJECTED cung khung gio phai cho phep (false)
        LocalDateTime afternoonStart = LocalDateTime.of(2026, 12, 10, 14, 0);
        LocalDateTime afternoonEnd = LocalDateTime.of(2026, 12, 10, 16, 0);
        createBooking(room, afternoonStart, afternoonEnd, BookingStatus.CANCELLED);
        createBooking(room, afternoonStart, afternoonEnd, BookingStatus.REJECTED);
        assertThat(overlaps(room.getId(), afternoonStart, afternoonEnd)).isFalse();
    }

    private boolean overlaps(Long roomId, LocalDateTime start, LocalDateTime end) {
        return bookingRepository.existsOverlappingBooking(roomId, start, end, BookingStatus.ACTIVE_STATUSES);
    }

    private Room createRoom(String code) {
        LocalDateTime now = LocalDateTime.now();
        Room r = new Room();
        r.setCode(code);
        r.setBuilding("B1");
        r.setCapacity(12);
        r.setEquipment("Bảng trắng");
        r.setStatus(RoomStatus.AVAILABLE);
        r.setCreatedAt(now);
        r.setUpdatedAt(now);
        return roomRepository.save(r);
    }

    private User createRequester(String email) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash("hashed");
        u.setFullName("Student");
        u.setRole(Role.STUDENT);
        u.setEnabled(true);
        u.setCreatedAt(LocalDateTime.now());
        return userRepository.save(u);
    }

    private Booking createBooking(Room targetRoom, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        LocalDateTime now = LocalDateTime.now();
        Booking b = new Booking();
        b.setRoom(targetRoom);
        b.setGroupId(10L);
        b.setRequestedByUserId(requester.getId());
        b.setStartTime(start);
        b.setEndTime(end);
        b.setPurpose("Học nhóm");
        b.setStatus(status);
        b.setCreatedAt(now);
        b.setUpdatedAt(now);
        return bookingRepository.save(b);
    }
}
