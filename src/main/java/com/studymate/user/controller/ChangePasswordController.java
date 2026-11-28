package com.studymate.user.controller;

import com.studymate.common.security.SecurityUtils;
import com.studymate.user.dto.ChangePasswordRequest;
import com.studymate.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/change-password")
public class ChangePasswordController {

    private final UserService userService;

    public ChangePasswordController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showChangePasswordForm(Model model) {
        requireCurrentUserId();
        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordRequest());
        }
        return "user/change-password";
    }

    @PostMapping
    public String changePassword(@Valid @ModelAttribute("changePasswordForm") ChangePasswordRequest form,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        Long userId = requireCurrentUserId();

        if (!bindingResult.hasErrors()) {
            try {
                userService.changePassword(
                        userId,
                        form.getCurrentPassword(),
                        form.getNewPassword(),
                        form.getConfirmPassword()
                );
                redirectAttributes.addFlashAttribute("successMessage", "Đã đổi mật khẩu thành công.");
                return "redirect:/change-password";
            } catch (IllegalArgumentException ex) {
                bindingResult.reject("password.invalid", ex.getMessage());
            }
        }

        // Xoa mat khau da nhap truoc khi tra lai view
        form.setCurrentPassword(null);
        form.setNewPassword(null);
        form.setConfirmPassword(null);
        return "user/change-password";
    }

    private Long requireCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Chưa đăng nhập"));
    }
}
