package com.studymate.user.controller;

import com.studymate.user.dto.RegisterRequest;
import com.studymate.user.service.UserService;
import com.studymate.user.service.UserService.EmailAlreadyExistsException;
import com.studymate.user.service.UserService.PasswordMismatchException;
import com.studymate.user.service.UserService.PasswordTooLongException;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            Model model) {

        // Kiem tra loi validation truoc
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(request);
            return "redirect:/login?registered";
        } catch (PasswordTooLongException e) {
            bindingResult.rejectValue("password", "tooLong", e.getMessage());
            return "auth/register";
        } catch (PasswordMismatchException e) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Mật khẩu nhập lại không khớp");
            return "auth/register";
        } catch (EmailAlreadyExistsException e) {
            bindingResult.rejectValue("email", "duplicate", "Email này đã được sử dụng");
            return "auth/register";
        } catch (DataIntegrityViolationException e) {
            // Hai request cung email chay dong thoi vuot qua kiem tra existsByEmail
            bindingResult.rejectValue("email", "duplicate", "Email này đã được sử dụng");
            return "auth/register";
        }
    }

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("email", userDetails.getUsername());
        // Lay role tu authority dang "ROLE_STUDENT" -> hien thi "STUDENT"
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("UNKNOWN");
        model.addAttribute("role", role);
        return "index";
    }
}
