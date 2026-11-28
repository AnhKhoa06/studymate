package com.studymate.user.service;

import com.studymate.common.security.CurrentUser;
import com.studymate.user.entity.User;
import com.studymate.user.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Spring Security goi phuong thuc nay bang email (username = email trong he thong nay)
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = (email == null) ? "" : email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Khong tim thay tai khoan"));

        // Authority dang "ROLE_STUDENT", "ROLE_MANAGER", "ROLE_ADMIN"
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        // Tra ve CurrentUser ke thua org.springframework.security.core.userdetails.User
        // enabled=false se bi chan dang nhap
        return new CurrentUser(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getRole(),
                user.isEnabled(),
                !user.isTemporarilyLocked(),
                authorities
        );
    }
}
