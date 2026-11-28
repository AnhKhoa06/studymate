package com.studymate.user.controller;

import com.studymate.user.dto.ActivityLogDto;
import com.studymate.user.service.ActivityLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLogController {

    private static final int PAGE_SIZE = 15;
    private static final List<String> KNOWN_ACTIONS = List.of(
            "LOGIN_SUCCESS",
            "LOGIN_FAILED",
            "ACCOUNT_LOCKED_TEMP",
            "ADMIN_CREATE_USER",
            "ADMIN_TOGGLE_STATUS",
            "ADMIN_CHANGE_ROLE",
            "PASSWORD_RESET_REQUEST",
            "PASSWORD_RESET_SUCCESS"
    );

    private final ActivityLogService activityLogService;

    public AdminLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public String listLogs(@RequestParam(name = "q", required = false) String keyword,
                           @RequestParam(name = "action", required = false) String action,
                           @RequestParam(name = "page", defaultValue = "0") int page,
                           Model model) {
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ActivityLogDto> logsPage = activityLogService.findLogs(keyword, action, pageable);

        model.addAttribute("logsPage", logsPage);
        model.addAttribute("keyword", keyword != null ? keyword.trim() : "");
        model.addAttribute("selectedAction", action != null ? action.trim() : "");
        model.addAttribute("actions", KNOWN_ACTIONS);
        return "admin/logs";
    }
}
