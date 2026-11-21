package com.studymate.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Khong co truong role: tai khoan tu dang ky luon la STUDENT, tranh mass assignment
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email tối đa 255 ký tự")
    private String email;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    // Kiem tra do dai byte duoc thuc hien trong service (BCrypt gioi han 72 byte UTF-8)
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự")
    private String password;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu")
    private String confirmPassword;

    public void setEmail(String email) {
        this.email = (email != null) ? email.trim() : null;
    }
}
