package com.studymate.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Cac endpoint nay chi de kiem thu phan quyen o giai doan 0A, se xoa sau
@Controller
public class PingController {

    @GetMapping("/admin/ping")
    public String adminPing() {
        return "admin/ping";
    }

    @GetMapping("/manager/ping")
    public String managerPing() {
        return "manager/ping";
    }
}
