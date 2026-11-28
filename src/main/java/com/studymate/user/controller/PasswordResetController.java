package com.studymate.user.controller;

import com.studymate.user.dto.ForgotPasswordRequest;
import com.studymate.user.dto.ResetPasswordRequest;
import com.studymate.user.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot-password")
    public String showForgotPassword(Model model) {
        if (!model.containsAttribute("forgotForm")) {
            model.addAttribute("forgotForm", new ForgotPasswordRequest());
        }
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String submitForgotPassword(@Valid @ModelAttribute("forgotForm") ForgotPasswordRequest form,
                                       BindingResult bindingResult,
                                       HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }
        String resetBaseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath("/reset-password")
                .replaceQuery(null)
                .build()
                .toUriString();
        passwordResetService.requestPasswordReset(form.getEmail(), resetBaseUrl);
        redirectAttributes.addFlashAttribute("infoMessage",
                "Nếu email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu đã được gửi.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam(name = "token", required = false) String token, Model model) {
        boolean valid = passwordResetService.isValidToken(token);
        model.addAttribute("tokenValid", valid);
        if (!model.containsAttribute("resetForm")) {
            ResetPasswordRequest form = new ResetPasswordRequest();
            form.setToken(token);
            model.addAttribute("resetForm", form);
        }
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String submitResetPassword(@Valid @ModelAttribute("resetForm") ResetPasswordRequest form,
                                      BindingResult bindingResult,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                passwordResetService.resetPassword(form.getToken(), form.getNewPassword(), form.getConfirmPassword());
                redirectAttributes.addFlashAttribute("resetSuccess", true);
                return "redirect:/login?reset";
            } catch (IllegalArgumentException ex) {
                bindingResult.reject("reset.invalid", ex.getMessage());
            }
        }
        model.addAttribute("tokenValid", passwordResetService.isValidToken(form.getToken()));
        return "auth/reset-password";
    }
}
