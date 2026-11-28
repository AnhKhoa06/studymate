package com.studymate.room;

import com.studymate.room.dto.RoomDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void searchRooms_filtersByKeywordBuildingStatusAndPaginates() {
        createRoom("B1-101", "B1", 10, "Máy chiếu, Bảng trắng", RoomStatus.AVAILABLE);
        createRoom("B1-102", "B1", 20, "TV, Ổ cắm", RoomStatus.MAINTENANCE);
        createRoom("A2-301", "A2", 15, "Máy chiếu", RoomStatus.AVAILABLE);

        var pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.asc("code")));
        Page<RoomDto> filtered = roomRepository.searchRooms("máy chiếu", null, RoomStatus.AVAILABLE, pageable);

        assertThat(filtered.getTotalElements()).isEqualTo(2);
        assertThat(filtered.getTotalPages()).isEqualTo(2);
        assertThat(filtered.getContent()).extracting(RoomDto::code).containsExactly("A2-301");

        Page<RoomDto> buildingB1 = roomRepository.searchRooms(null, "B1", null, PageRequest.of(0, 10));
        assertThat(buildingB1.getTotalElements()).isEqualTo(2);
        assertThat(roomRepository.findDistinctBuildings()).containsExactly("A2", "B1");
    }

    @Test
    void findAvailableRooms_excludesNonAvailableBookedRoomsAndRespectsMinCapacity() {
        User requester = createRequester("repo.student@studymate.test");
        Room tooSmall = createRoom("B1-101", "B1", 8, "Bảng trắng", RoomStatus.AVAILABLE);
        Room freeRoom = createRoom("B1-102", "B1", 16, "Máy chiếu", RoomStatus.AVAILABLE);
        Room bookedRoom = createRoom("B1-104", "B1", 16, "Máy chiếu", RoomStatus.AVAILABLE);
        Room cancelledRoom = createRoom("B1-105", "B1", 16, "Máy chiếu", RoomStatus.AVAILABLE);
        createRoom("B1-103", "B1", 24, "Máy chiếu", RoomStatus.MAINTENANCE);

        LocalDateTime start = LocalDateTime.of(2026, 12, 1, 8, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 1, 10, 0);

        createBooking(bookedRoom, requester.getId(), start.plusMinutes(30), end.plusMinutes(30), BookingStatus.APPROVED);
        createBooking(cancelledRoom, requester.getId(), start, end, BookingStatus.CANCELLED);

        List<RoomDto> result = roomRepository.findAvailableRooms(start, end, BookingStatus.ACTIVE_STATUSES, 10);
        assertThat(result).extracting(RoomDto::code).containsExactly("B1-102", "B1-105");
        assertThat(tooSmall.getCapacity()).isLessThan(10);
    }

    private User createRequester(String email) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash("hashed");
        u.setFullName("Student Repo");
        u.setRole(Role.STUDENT);
        u.setEnabled(true);
        u.setCreatedAt(LocalDateTime.now());
        return userRepository.save(u);
    }

    private Booking createBooking(Room room, Long userId, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        LocalDateTime now = LocalDateTime.now();
        Booking b = new Booking();
        b.setRoom(room);
        b.setGroupId(1L);
        b.setRequestedByUserId(userId);
        b.setStartTime(start);
        b.setEndTime(end);
        b.setPurpose("Ôn tập");
        b.setStatus(status);
        b.setCreatedAt(now);
        b.setUpdatedAt(now);
        return bookingRepository.save(b);
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
