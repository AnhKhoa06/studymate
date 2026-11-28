package com.studymate.room.controller;

import com.studymate.common.security.SecurityUtils;
import com.studymate.room.dto.BookingDto;
import com.studymate.room.entity.BookingStatus;
import com.studymate.room.service.BookingService;
import com.studymate.room.state.InvalidBookingStateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/bookings")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerBookingController {

    private static final int PAGE_SIZE = 10;

    private final BookingService bookingService;

    public ManagerBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public String listBookings(@RequestParam(name = "q", required = false) String keyword,
                               @RequestParam(name = "status", required = false) String statusParam,
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               Model model) {
        BookingStatus selectedStatus = parseStatusFilter(statusParam);
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(
                safePage,
                PAGE_SIZE,
                Sort.by(Sort.Order.asc("startTime"), Sort.Order.desc("id"))
        );

        Page<BookingDto> bookingsPage = bookingService.searchBookingsForManager(selectedStatus, keyword, pageable);

        model.addAttribute("bookingsPage", bookingsPage);
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("keyword", keyword != null ? keyword.trim() : "");
        model.addAttribute("selectedStatus", selectedStatus);
        return "manager/bookings";
    }

    @PostMapping("/{id}/approve")
    public String approveBooking(@PathVariable("id") Long id,
                                 @RequestParam(name = "q", required = false) String keyword,
                                 @RequestParam(name = "status", required = false) String statusParam,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 RedirectAttributes redirectAttributes) {
        Long managerId = requireCurrentUserId();
        try {
            bookingService.approveBooking(id, managerId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt yêu cầu đặt phòng.");
        } catch (BookingService.BookingNotFoundException | InvalidBookingStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        addRedirectFilterParams(redirectAttributes, keyword, statusParam, page);
        return "redirect:/manager/bookings";
    }

    @PostMapping("/{id}/reject")
    public String rejectBooking(@PathVariable("id") Long id,
                                @RequestParam(name = "reason", required = false) String reason,
                                @RequestParam(name = "q", required = false) String keyword,
                                @RequestParam(name = "status", required = false) String statusParam,
                                @RequestParam(name = "page", defaultValue = "0") int page,
                                RedirectAttributes redirectAttributes) {
        Long managerId = requireCurrentUserId();
        try {
            bookingService.rejectBooking(id, managerId, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối yêu cầu đặt phòng.");
        } catch (BookingService.BookingNotFoundException
                 | InvalidBookingStateException
                 | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        addRedirectFilterParams(redirectAttributes, keyword, statusParam, page);
        return "redirect:/manager/bookings";
    }

    private BookingStatus parseStatusFilter(String statusParam) {
        if (statusParam == null) {
            return BookingStatus.PENDING;
        }
        String trimmed = statusParam.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return BookingStatus.valueOf(trimmed);
        } catch (IllegalArgumentException ex) {
            return BookingStatus.PENDING;
        }
    }

    private void addRedirectFilterParams(RedirectAttributes redirectAttributes,
                                         String keyword,
                                         String statusParam,
                                         int page) {
        if (keyword != null && !keyword.isBlank()) {
            redirectAttributes.addAttribute("q", keyword.trim());
        }
        if (statusParam != null) {
            redirectAttributes.addAttribute("status", statusParam.trim());
        }
        if (page > 0) {
            redirectAttributes.addAttribute("page", page);
        }
    }

    private Long requireCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Chưa đăng nhập"));
    }
}
