package com.studymate.room.service;

import com.studymate.room.dto.BookingCreateRequest;
import com.studymate.room.dto.BookingDto;
import com.studymate.room.entity.Booking;
import com.studymate.room.entity.BookingStatus;
import com.studymate.room.entity.Room;
import com.studymate.room.entity.RoomStatus;
import com.studymate.room.event.BookingApprovedEvent;
import com.studymate.room.event.BookingCancelledEvent;
import com.studymate.room.event.BookingRejectedEvent;
import com.studymate.room.repository.BookingRepository;
import com.studymate.room.repository.RoomRepository;
import com.studymate.room.state.BookingStateFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final GroupLeaderChecker groupLeaderChecker;
    private final ApplicationEventPublisher eventPublisher;

    public BookingService(BookingRepository bookingRepository,
                          RoomRepository roomRepository,
                          GroupLeaderChecker groupLeaderChecker,
                          ApplicationEventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.groupLeaderChecker = groupLeaderChecker;
        this.eventPublisher = eventPublisher;
    }

    // Dung READ_COMMITTED + khoa bi quan FOR UPDATE tren ban ghi Room de hai giao dich dong thoi
    // cho nhau va luon doc duoc booking vua commit, tranh dat trung phong.
    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BookingDto createBooking(Long currentUserId, BookingCreateRequest request) {
        if (currentUserId == null) {
            throw new AccessDeniedException("Chưa đăng nhập");
        }
        validateBookingTime(request);

        if (!groupLeaderChecker.isGroupLeader(currentUserId, request.getGroupId())) {
            throw new AccessDeniedException("Chỉ trưởng nhóm mới có quyền đặt phòng cho nhóm");
        }

        Room room = roomRepository.findByIdForUpdate(request.getRoomId())
                .orElseThrow(() -> new RoomService.RoomNotFoundException("Không tìm thấy phòng học"));

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new RoomUnavailableException("Phòng học hiện không sẵn sàng để đặt");
        }

        boolean overlapped = bookingRepository.existsOverlappingBooking(
                room.getId(),
                request.getStartTime(),
                request.getEndTime(),
                BookingStatus.ACTIVE_STATUSES
        );
        if (overlapped) {
            throw new BookingConflictException("Phòng học đã có lịch đặt trong khung giờ này");
        }

        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setGroupId(request.getGroupId());
        booking.setRequestedByUserId(currentUserId);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setPurpose(request.getPurpose() != null ? request.getPurpose().trim() : "");
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);

        Booking saved = bookingRepository.save(booking);
        return toDto(saved);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public BookingDto approveBooking(Long bookingId, Long managerUserId) {
        if (managerUserId == null) {
            throw new AccessDeniedException("Chưa đăng nhập");
        }
        Booking booking = findBookingOrThrow(bookingId);
        BookingStateFactory.from(booking.getStatus()).approve(booking, managerUserId, LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        eventPublisher.publishEvent(new BookingApprovedEvent(
                saved.getId(),
                saved.getRoom().getId(),
                saved.getRoom().getCode(),
                saved.getGroupId(),
                saved.getRequestedByUserId(),
                managerUserId,
                saved.getStartTime(),
                saved.getEndTime()
        ));
        return toDto(saved);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public BookingDto rejectBooking(Long bookingId, Long managerUserId, String reason) {
        if (managerUserId == null) {
            throw new AccessDeniedException("Chưa đăng nhập");
        }
        Booking booking = findBookingOrThrow(bookingId);
        BookingStateFactory.from(booking.getStatus()).reject(booking, managerUserId, reason, LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        eventPublisher.publishEvent(new BookingRejectedEvent(
                saved.getId(),
                saved.getRoom().getId(),
                saved.getRoom().getCode(),
                saved.getGroupId(),
                saved.getRequestedByUserId(),
                managerUserId,
                saved.getRejectionReason(),
                saved.getStartTime(),
                saved.getEndTime()
        ));
        return toDto(saved);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @Transactional
    public BookingDto cancelBooking(Long bookingId, Long currentUserId) {
        if (currentUserId == null) {
            throw new AccessDeniedException("Chưa đăng nhập");
        }
        Booking booking = findBookingOrThrow(bookingId);
        boolean isOwner = Objects.equals(booking.getRequestedByUserId(), currentUserId)
                || groupLeaderChecker.isGroupLeader(currentUserId, booking.getGroupId());
        if (!isOwner) {
            throw new AccessDeniedException("Bạn không có quyền hủy đặt phòng của nhóm khác");
        }
        BookingStateFactory.from(booking.getStatus()).cancel(booking, LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        eventPublisher.publishEvent(new BookingCancelledEvent(
                saved.getId(),
                saved.getRoom().getId(),
                saved.getRoom().getCode(),
                saved.getGroupId(),
                currentUserId,
                saved.getStartTime(),
                saved.getEndTime()
        ));
        return toDto(saved);
    }

    // Tai lich dat trong ngay cua nhieu phong bang 1 truy van gom nhom theo roomId, tranh N+1
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Map<Long, List<BookingDto>> findDailySchedulesForRooms(Collection<Long> roomIds, LocalDate date) {
        if (roomIds == null || roomIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDateTime dayStart = targetDate.atStartOfDay();
        LocalDateTime dayEnd = targetDate.plusDays(1).atStartOfDay();
        return bookingRepository.findBookingsForRoomsInRange(roomIds, dayStart, dayEnd, BookingStatus.ACTIVE_STATUSES)
                .stream()
                .collect(Collectors.groupingBy(BookingDto::roomId));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public Page<BookingDto> findStudentBookings(Long userId, BookingStatus status, Pageable pageable) {
        if (userId == null) {
            throw new AccessDeniedException("Chưa đăng nhập");
        }
        return bookingRepository.findByRequestedByUserIdAndStatus(userId, status, pageable);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional(readOnly = true)
    public Page<BookingDto> searchBookingsForManager(BookingStatus status, String keyword, Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        return bookingRepository.searchBookings(status, normalizedKeyword, pageable);
    }

    private Booking findBookingOrThrow(Long bookingId) {
        return bookingRepository.findByIdWithRoom(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Không tìm thấy đơn đặt phòng"));
    }

    private void validateBookingTime(BookingCreateRequest request) {
        if (request == null || request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Thời gian đặt phòng không hợp lệ");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Không thể đặt phòng trong quá khứ");
        }
    }

    private BookingDto toDto(Booking booking) {
        Room room = booking.getRoom();
        return new BookingDto(
                booking.getId(),
                room.getId(),
                room.getCode(),
                room.getBuilding(),
                booking.getGroupId(),
                booking.getRequestedByUserId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose(),
                booking.getStatus(),
                booking.getRejectionReason(),
                booking.getReviewedByUserId(),
                booking.getReviewedAt(),
                booking.getCreatedAt()
        );
    }

    public static class BookingConflictException extends RuntimeException {
        public BookingConflictException(String message) {
            super(message);
        }
    }

    public static class RoomUnavailableException extends RuntimeException {
        public RoomUnavailableException(String message) {
            super(message);
        }
    }

    public static class BookingNotFoundException extends RuntimeException {
        public BookingNotFoundException(String message) {
            super(message);
        }
    }
}
