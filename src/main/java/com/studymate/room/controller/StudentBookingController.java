package com.studymate.room.controller;

import com.studymate.common.security.SecurityUtils;
import com.studymate.room.dto.BookingCreateRequest;
import com.studymate.room.dto.BookingDto;
import com.studymate.room.entity.BookingStatus;
import com.studymate.room.service.BookingService;
import com.studymate.room.service.RoomService;
import com.studymate.room.state.InvalidBookingStateException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/bookings")
@PreAuthorize("hasRole('STUDENT')")
public class StudentBookingController {

    private static final int PAGE_SIZE = 10;

    private final BookingService bookingService;
    private final RoomService roomService;

    public StudentBookingController(BookingService bookingService, RoomService roomService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
    }

    @GetMapping
    public String viewBookings(@RequestParam(name = "status", required = false) BookingStatus status,
                               @RequestParam(name = "roomId", required = false) Long roomId,
                               @RequestParam(name = "date", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               Model model) {
        Long userId = requireCurrentUserId();
        if (!model.containsAttribute("bookingForm")) {
            BookingCreateRequest form = new BookingCreateRequest();
            form.setRoomId(roomId);
            if (date != null) {
                form.setStartTime(date.atTime(8, 0));
                form.setEndTime(date.atTime(10, 0));
            }
            model.addAttribute("bookingForm", form);
        }
        populateModel(model, userId, status, page);
        return "room/bookings";
    }

    @PostMapping
    public String createBooking(@Valid @ModelAttribute("bookingForm") BookingCreateRequest form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long userId = requireCurrentUserId();
        if (!bindingResult.hasErrors()) {
            try {
                BookingDto created = bookingService.createBooking(userId, form);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã gửi yêu cầu đặt phòng " + created.roomCode() + " (chờ quản lý duyệt).");
                return "redirect:/bookings";
            } catch (BookingService.BookingConflictException
                     | BookingService.RoomUnavailableException
                     | RoomService.RoomNotFoundException
                     | IllegalArgumentException ex) {
                bindingResult.reject("booking.error", ex.getMessage());
            }
        }
        populateModel(model, userId, null, 0);
        return "room/bookings";
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable("id") Long id,
                                @RequestParam(name = "status", required = false) BookingStatus status,
                                @RequestParam(name = "page", defaultValue = "0") int page,
                                RedirectAttributes redirectAttributes) {
        Long userId = requireCurrentUserId();
        try {
            bookingService.cancelBooking(id, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đặt phòng thành công.");
        } catch (BookingService.BookingNotFoundException | InvalidBookingStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        if (status != null) {
            redirectAttributes.addAttribute("status", status.name());
        }
        if (page > 0) {
            redirectAttributes.addAttribute("page", page);
        }
        return "redirect:/bookings";
    }

    private void populateModel(Model model, Long userId, BookingStatus status, int page) {
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookingDto> bookingsPage = bookingService.findStudentBookings(userId, status, pageable);

        model.addAttribute("bookingsPage", bookingsPage);
        model.addAttribute("availableRooms", roomService.listAvailableRoomsForSelection());
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("selectedStatus", status);
    }

    private Long requireCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Chưa đăng nhập"));
    }
}
