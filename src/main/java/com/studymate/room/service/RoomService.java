package com.studymate.room.service;

import com.studymate.room.dto.RoomDto;
import com.studymate.room.dto.RoomFormRequest;
import com.studymate.room.entity.BookingStatus;
import com.studymate.room.entity.Room;
import com.studymate.room.entity.RoomStatus;
import com.studymate.room.repository.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional(readOnly = true)
    public Page<RoomDto> searchRooms(String keyword, String building, RoomStatus status, Pageable pageable) {
        String normalizedKeyword = normalizeBlankToNull(keyword);
        String normalizedBuilding = normalizeBlankToNull(building);
        return roomRepository.searchRooms(normalizedKeyword, normalizedBuilding, status, pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<String> listDistinctBuildings() {
        return roomRepository.findDistinctBuildings();
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional(readOnly = true)
    public RoomDto getRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Không tìm thấy phòng học"));
        return toDto(room);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public RoomDto createRoom(RoomFormRequest request) {
        String normalizedCode = normalizeCode(request.getCode());
        if (roomRepository.existsByCode(normalizedCode)) {
            throw new DuplicateRoomCodeException("Mã phòng này đã tồn tại");
        }

        LocalDateTime now = LocalDateTime.now();
        Room room = new Room();
        applyFormToEntity(room, request, normalizedCode);
        room.setCreatedAt(now);
        room.setUpdatedAt(now);

        return toDto(roomRepository.save(room));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public RoomDto updateRoom(Long roomId, RoomFormRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Không tìm thấy phòng học"));

        String normalizedCode = normalizeCode(request.getCode());
        if (roomRepository.existsByCodeAndIdNot(normalizedCode, roomId)) {
            throw new DuplicateRoomCodeException("Mã phòng này đã tồn tại");
        }

        applyFormToEntity(room, request, normalizedCode);
        room.setUpdatedAt(LocalDateTime.now());

        return toDto(roomRepository.save(room));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public void updateRoomStatus(Long roomId, RoomStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Trạng thái phòng không hợp lệ");
        }
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Không tìm thấy phòng học"));
        room.setStatus(newStatus);
        room.setUpdatedAt(LocalDateTime.now());
        roomRepository.save(room);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Page<RoomDto> searchAvailableRooms(String keyword, String building, Integer minCapacity, Pageable pageable) {
        if (minCapacity != null && minCapacity < 1) {
            throw new IllegalArgumentException("Sức chứa tối thiểu phải lớn hơn 0");
        }
        return roomRepository.searchAvailableRooms(
                normalizeBlankToNull(keyword),
                normalizeBlankToNull(building),
                minCapacity,
                pageable
        );
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<RoomDto> listAvailableRoomsForSelection() {
        return roomRepository.findAllAvailableOrderByBuildingAndCode();
    }

    // Chữ ký hợp đồng cho module insight (Khang) và kiểm tra phòng trống theo khung giờ.
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<RoomDto> findAvailableRooms(LocalDateTime startTime, LocalDateTime endTime, Integer minCapacity) {
        validateTimeRangeAndCapacity(startTime, endTime, minCapacity);
        return roomRepository.findAvailableRooms(startTime, endTime, BookingStatus.ACTIVE_STATUSES, minCapacity);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Page<RoomDto> findAvailableRooms(LocalDateTime startTime,
                                            LocalDateTime endTime,
                                            Integer minCapacity,
                                            String building,
                                            Pageable pageable) {
        validateTimeRangeAndCapacity(startTime, endTime, minCapacity);
        return roomRepository.findAvailableRoomsPage(
                startTime,
                endTime,
                BookingStatus.ACTIVE_STATUSES,
                minCapacity,
                normalizeBlankToNull(building),
                pageable
        );
    }

    private void validateTimeRangeAndCapacity(LocalDateTime startTime, LocalDateTime endTime, Integer minCapacity) {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Khoảng thời gian tìm phòng không hợp lệ");
        }
        if (minCapacity != null && minCapacity < 1) {
            throw new IllegalArgumentException("Sức chứa tối thiểu phải lớn hơn 0");
        }
    }

    private void applyFormToEntity(Room room, RoomFormRequest request, String normalizedCode) {
        room.setCode(normalizedCode);
        room.setBuilding(request.getBuilding().trim());
        room.setCapacity(request.getCapacity());
        room.setEquipment(request.getEquipment() != null ? request.getEquipment().trim() : "");
        room.setStatus(request.getStatus());
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeBlankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private RoomDto toDto(Room room) {
        return new RoomDto(
                room.getId(),
                room.getCode(),
                room.getBuilding(),
                room.getCapacity(),
                room.getEquipment(),
                room.getStatus()
        );
    }

    public static class DuplicateRoomCodeException extends RuntimeException {
        public DuplicateRoomCodeException(String message) {
            super(message);
        }
    }

    public static class RoomNotFoundException extends RuntimeException {
        public RoomNotFoundException(String message) {
            super(message);
        }
    }
}
