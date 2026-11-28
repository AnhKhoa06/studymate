package com.studymate.user.controller;

import com.studymate.common.security.SecurityUtils;
import com.studymate.user.dto.AdminCreateUserRequest;
import com.studymate.user.dto.AdminUserDto;
import com.studymate.user.entity.Role;
import com.studymate.user.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private static final int PAGE_SIZE = 10;

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public String listUsers(@RequestParam(name = "q", required = false) String keyword,
                            @RequestParam(name = "role", required = false) Role role,
                            @RequestParam(name = "enabled", required = false) Boolean enabled,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            Model model) {
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminUserDto> usersPage = adminUserService.findUsers(keyword, role, enabled, pageable);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("keyword", keyword != null ? keyword.trim() : "");
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedEnabled", enabled);
        model.addAttribute("roles", Role.values());
        model.addAttribute("currentAdminId", requireCurrentUserId());
        if (!model.containsAttribute("createUserForm")) {
            model.addAttribute("createUserForm", new AdminCreateUserRequest());
        }
        return "admin/users";
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("createUserForm") AdminCreateUserRequest form,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", message != null ? message : "Dữ liệu không hợp lệ");
            return "redirect:/admin/users";
        }
        try {
            adminUserService.createUser(form, requireCurrentUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo tài khoản mới thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Long id,
                               @RequestParam(name = "q", required = false) String keyword,
                               @RequestParam(name = "role", required = false) Role role,
                               @RequestParam(name = "enabled", required = false) Boolean enabled,
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               RedirectAttributes redirectAttributes) {
        try {
            boolean nowEnabled = adminUserService.toggleUserEnabled(id, requireCurrentUserId());
            redirectAttributes.addFlashAttribute("successMessage",
                    nowEnabled ? "Đã mở khóa tài khoản." : "Đã khóa tài khoản.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return buildRedirectUrl(keyword, role, enabled, page, redirectAttributes);
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable("id") Long id,
                             @RequestParam("newRole") Role newRole,
                             @RequestParam(name = "q", required = false) String keyword,
                             @RequestParam(name = "role", required = false) Role role,
                             @RequestParam(name = "enabled", required = false) Boolean enabled,
                             @RequestParam(name = "page", defaultValue = "0") int page,
                             RedirectAttributes redirectAttributes) {
        try {
            adminUserService.changeUserRole(id, newRole, requireCurrentUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật vai trò tài khoản.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return buildRedirectUrl(keyword, role, enabled, page, redirectAttributes);
    }

    private String buildRedirectUrl(String keyword, Role role, Boolean enabled, int page,
                                    RedirectAttributes redirectAttributes) {
        if (keyword != null && !keyword.isBlank()) {
            redirectAttributes.addAttribute("q", keyword.trim());
        }
        if (role != null) {
            redirectAttributes.addAttribute("role", role.name());
        }
        if (enabled != null) {
            redirectAttributes.addAttribute("enabled", enabled);
        }
        if (page > 0) {
            redirectAttributes.addAttribute("page", page);
        }
        return "redirect:/admin/users";
    }

    private Long requireCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Chưa đăng nhập"));
    }
}
