package com.studymate.common.security;

import com.studymate.user.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;

public class CurrentUser extends User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String fullName;
    private final Role role;

    public CurrentUser(
            Long id,
            String email,
            String passwordHash,
            String fullName,
            Role role,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities) {
        super(email, passwordHash, enabled, true, true, true, authorities);
        this.id = id;
        this.fullName = fullName;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public String getEmail() {
        return getUsername();
    }
}
