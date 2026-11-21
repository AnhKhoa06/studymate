package com.studymate.common.security;

import com.studymate.user.entity.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<CurrentUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CurrentUser currentUser) {
            return Optional.of(currentUser);
        }
        return Optional.empty();
    }

    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(CurrentUser::getId);
    }

    public static Optional<Role> getCurrentRole() {
        return getCurrentUser().map(CurrentUser::getRole);
    }

    public static Optional<CurrentUserView> getCurrentUserView() {
        return getCurrentUser().map(user -> new CurrentUserView(user.getFullName(), user.getRole().name()));
    }
}
