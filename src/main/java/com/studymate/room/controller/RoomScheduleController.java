package com.studymate.room.controller;

import com.studymate.room.dto.BookingDto;
import com.studymate.room.dto.RoomDto;
import com.studymate.room.service.BookingService;
import com.studymate.room.service.RoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rooms")
@PreAuthorize("isAuthenticated()")
public class RoomScheduleController {

    private static final int PAGE_SIZE = 10;

    private final RoomService roomService;
    private final BookingService bookingService;

    public RoomScheduleController(RoomService roomService, BookingService bookingService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public String viewDailySchedule(@RequestParam(name = "date", required = false)
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                    @RequestParam(name = "q", required = false) String keyword,
                                    @RequestParam(name = "building", required = false) String building,
                                    @RequestParam(name = "minCapacity", required = false) Integer minCapacity,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    Model model) {
        LocalDate selectedDate = date != null ? date : LocalDate.now();
        Integer safeMinCapacity = (minCapacity != null && minCapacity > 0) ? minCapacity : null;
        int safePage = Math.max(0, page);

        Pageable pageable = PageRequest.of(
                safePage,
                PAGE_SIZE,
                Sort.by(Sort.Order.asc("building"), Sort.Order.asc("code"))
        );

        Page<RoomDto> roomsPage = roomService.searchAvailableRooms(keyword, building, safeMinCapacity, pageable);
        List<Long> roomIds = roomsPage.getContent().stream().map(RoomDto::id).toList();
        Map<Long, List<BookingDto>> schedulesByRoomId = bookingService.findDailySchedulesForRooms(roomIds, selectedDate);

        model.addAttribute("roomsPage", roomsPage);
        model.addAttribute("schedulesByRoomId", schedulesByRoomId);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("buildings", roomService.listDistinctBuildings());
        model.addAttribute("keyword", keyword != null ? keyword.trim() : "");
        model.addAttribute("selectedBuilding", building != null ? building.trim() : "");
        model.addAttribute("minCapacity", safeMinCapacity);
        return "room/rooms";
    }
}
