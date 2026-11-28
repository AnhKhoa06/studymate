package com.studymate.room.repository;

import com.studymate.room.dto.RoomDto;
import com.studymate.room.entity.BookingStatus;
import com.studymate.room.entity.Room;
import com.studymate.room.entity.RoomStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    // Khoa dong phong trong transaction dat phong de chong tranh chap dat trung
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT new com.studymate.room.dto.RoomDto(
                r.id, r.code, r.building, r.capacity, r.equipment, r.status
            )
            FROM Room r
            WHERE (:keyword IS NULL
                   OR LOWER(r.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(r.building) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(r.equipment) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:building IS NULL OR r.building = :building)
              AND (:status IS NULL OR r.status = :status)
            """)
    Page<RoomDto> searchRooms(@Param("keyword") String keyword,
                              @Param("building") String building,
                              @Param("status") RoomStatus status,
                              Pageable pageable);

    @Query("SELECT DISTINCT r.building FROM Room r ORDER BY r.building ASC")
    List<String> findDistinctBuildings();

    @Query("""
            SELECT new com.studymate.room.dto.RoomDto(
                r.id, r.code, r.building, r.capacity, r.equipment, r.status
            )
            FROM Room r
            WHERE r.status = com.studymate.room.entity.RoomStatus.AVAILABLE
              AND (:minCapacity IS NULL OR r.capacity >= :minCapacity)
              AND NOT EXISTS (
                  SELECT 1 FROM Booking b
                  WHERE b.room = r
                    AND b.status IN :activeStatuses
                    AND b.startTime < :endTime
                    AND b.endTime > :startTime
              )
            ORDER BY r.building ASC, r.code ASC
            """)
    List<RoomDto> findAvailableRooms(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime,
                                     @Param("activeStatuses") Collection<BookingStatus> activeStatuses,
                                     @Param("minCapacity") Integer minCapacity);

    @Query("""
            SELECT new com.studymate.room.dto.RoomDto(
                r.id, r.code, r.building, r.capacity, r.equipment, r.status
            )
            FROM Room r
            WHERE r.status = com.studymate.room.entity.RoomStatus.AVAILABLE
              AND (:minCapacity IS NULL OR r.capacity >= :minCapacity)
              AND (:building IS NULL OR r.building = :building)
              AND NOT EXISTS (
                  SELECT 1 FROM Booking b
                  WHERE b.room = r
                    AND b.status IN :activeStatuses
                    AND b.startTime < :endTime
                    AND b.endTime > :startTime
              )
            """)
    Page<RoomDto> findAvailableRoomsPage(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime,
                                         @Param("activeStatuses") Collection<BookingStatus> activeStatuses,
                                         @Param("minCapacity") Integer minCapacity,
                                         @Param("building") String building,
                                         Pageable pageable);

    @Query("""
            SELECT new com.studymate.room.dto.RoomDto(
                r.id, r.code, r.building, r.capacity, r.equipment, r.status
            )
            FROM Room r
            WHERE r.status = com.studymate.room.entity.RoomStatus.AVAILABLE
              AND (:keyword IS NULL
                   OR LOWER(r.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(r.building) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(r.equipment) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:building IS NULL OR r.building = :building)
              AND (:minCapacity IS NULL OR r.capacity >= :minCapacity)
            """)
    Page<RoomDto> searchAvailableRooms(@Param("keyword") String keyword,
                                       @Param("building") String building,
                                       @Param("minCapacity") Integer minCapacity,
                                       Pageable pageable);

    @Query("""
            SELECT new com.studymate.room.dto.RoomDto(
                r.id, r.code, r.building, r.capacity, r.equipment, r.status
            )
            FROM Room r
            WHERE r.status = com.studymate.room.entity.RoomStatus.AVAILABLE
            ORDER BY r.building ASC, r.code ASC
            """)
    List<RoomDto> findAllAvailableOrderByBuildingAndCode();
}
