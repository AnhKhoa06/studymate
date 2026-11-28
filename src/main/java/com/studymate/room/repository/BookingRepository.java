package com.studymate.room.repository;

import com.studymate.room.dto.BookingDto;
import com.studymate.room.entity.Booking;
import com.studymate.room.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Kiem tra chong lan khung gio [startTime, endTime) voi cac trang thai dang giu phong
    @Query("""
            SELECT COUNT(b) > 0
            FROM Booking b
            WHERE b.room.id = :roomId
              AND b.status IN :activeStatuses
              AND b.startTime < :endTime
              AND b.endTime > :startTime
            """)
    boolean existsOverlappingBooking(@Param("roomId") Long roomId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime,
                                     @Param("activeStatuses") Collection<BookingStatus> activeStatuses);

    @Query("""
            SELECT b
            FROM Booking b
            JOIN FETCH b.room
            WHERE b.id = :id
            """)
    Optional<Booking> findByIdWithRoom(@Param("id") Long id);

    @Query("""
            SELECT new com.studymate.room.dto.BookingDto(
                b.id, r.id, r.code, r.building, b.groupId, b.requestedByUserId,
                b.startTime, b.endTime, b.purpose, b.status, b.rejectionReason,
                b.reviewedByUserId, b.reviewedAt, b.createdAt
            )
            FROM Booking b
            JOIN b.room r
            WHERE r.id = :roomId
              AND b.status IN :statuses
              AND b.startTime < :rangeEnd
              AND b.endTime > :rangeStart
            ORDER BY b.startTime ASC
            """)
    List<BookingDto> findRoomBookingsInRange(@Param("roomId") Long roomId,
                                             @Param("rangeStart") LocalDateTime rangeStart,
                                             @Param("rangeEnd") LocalDateTime rangeEnd,
                                             @Param("statuses") Collection<BookingStatus> statuses);

    @Query("""
            SELECT new com.studymate.room.dto.BookingDto(
                b.id, r.id, r.code, r.building, b.groupId, b.requestedByUserId,
                b.startTime, b.endTime, b.purpose, b.status, b.rejectionReason,
                b.reviewedByUserId, b.reviewedAt, b.createdAt
            )
            FROM Booking b
            JOIN b.room r
            WHERE r.id IN :roomIds
              AND b.status IN :statuses
              AND b.startTime < :rangeEnd
              AND b.endTime > :rangeStart
            ORDER BY b.startTime ASC
            """)
    List<BookingDto> findBookingsForRoomsInRange(@Param("roomIds") Collection<Long> roomIds,
                                                 @Param("rangeStart") LocalDateTime rangeStart,
                                                 @Param("rangeEnd") LocalDateTime rangeEnd,
                                                 @Param("statuses") Collection<BookingStatus> statuses);

    @Query("""
            SELECT new com.studymate.room.dto.BookingDto(
                b.id, r.id, r.code, r.building, b.groupId, b.requestedByUserId,
                b.startTime, b.endTime, b.purpose, b.status, b.rejectionReason,
                b.reviewedByUserId, b.reviewedAt, b.createdAt
            )
            FROM Booking b
            JOIN b.room r
            WHERE b.requestedByUserId = :userId
              AND (:status IS NULL OR b.status = :status)
            """)
    Page<BookingDto> findByRequestedByUserIdAndStatus(@Param("userId") Long userId,
                                                      @Param("status") BookingStatus status,
                                                      Pageable pageable);

    @Query("""
            SELECT new com.studymate.room.dto.BookingDto(
                b.id, r.id, r.code, r.building, b.groupId, b.requestedByUserId,
                b.startTime, b.endTime, b.purpose, b.status, b.rejectionReason,
                b.reviewedByUserId, b.reviewedAt, b.createdAt
            )
            FROM Booking b
            JOIN b.room r
            WHERE (:status IS NULL OR b.status = :status)
              AND (:keyword IS NULL
                   OR LOWER(r.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(r.building) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.purpose) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<BookingDto> searchBookings(@Param("status") BookingStatus status,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);

    @Query("""
            SELECT new com.studymate.room.dto.BookingDto(
                b.id, r.id, r.code, r.building, b.groupId, b.requestedByUserId,
                b.startTime, b.endTime, b.purpose, b.status, b.rejectionReason,
                b.reviewedByUserId, b.reviewedAt, b.createdAt
            )
            FROM Booking b
            JOIN b.room r
            WHERE b.requestedByUserId = :userId
            """)
    Page<BookingDto> findByRequestedByUserId(@Param("userId") Long userId, Pageable pageable);
}
