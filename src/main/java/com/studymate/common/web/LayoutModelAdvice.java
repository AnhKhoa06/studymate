package com.studymate.common.web;

import com.studymate.common.security.CurrentUserView;
import com.studymate.common.security.SecurityUtils;
import com.studymate.user.entity.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class LayoutModelAdvice {

    @ModelAttribute("currentUser")
    public CurrentUserView currentUser() {
        return SecurityUtils.getCurrentUserView().orElse(null);
    }

    @ModelAttribute("currentRole")
    public String currentRole(Authentication authentication) {
        Role role = SecurityUtils.getCurrentRole().orElse(null);
        if (role != null) {
            return role.name();
        }
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority ga : authentication.getAuthorities()) {
                String auth = ga.getAuthority();
                if (auth != null && auth.startsWith("ROLE_")) {
                    return auth.substring(5);
                }
            }
        }
        return null;
    }

    @ModelAttribute("menuItems")
    public List<MenuItem> menuItems(Authentication authentication,
                                    HttpServletRequest request) {
        Role role = SecurityUtils.getCurrentRole().orElse(null);
        if (role == null && authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority ga : authentication.getAuthorities()) {
                String auth = ga.getAuthority();
                if (auth != null && auth.startsWith("ROLE_")) {
                    try {
                        role = Role.valueOf(auth.substring(5));
                        break;
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }
        if (role == null) {
            return Collections.emptyList();
        }
        String path = request.getRequestURI();
        return MenuRegistry.getMenuItems(role).stream()
                .map(item -> item.withActive(isActive(path, item.url())))
                .toList();
    }

    /**
     * Trả true khi path bằng url hoặc bắt đầu bằng url + "/".
     * Quy tắc: "/" không làm mục nào active (tránh khớp tất cả).
     */
    static boolean isActive(String path, String url) {
        if (url == null || path == null) return false;
        if (url.equals("/")) return path.equals("/");
        return path.equals(url) || path.startsWith(url + "/");
    }
}
