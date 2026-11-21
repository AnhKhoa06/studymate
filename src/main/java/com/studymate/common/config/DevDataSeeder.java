package com.studymate.common.config;

import com.studymate.user.entity.Role;
import com.studymate.user.entity.User;
import com.studymate.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

// Chi chay o profile "local", khong chay khi test hay san xuat
@Configuration
@Profile("local")
public class DevDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Mat khau doc tu property app.seed.password, khong hard-code
    @Value("${app.seed.password:}")
    private String seedPassword;

    public DevDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public ApplicationRunner seedDevData() {
        return args -> {
            if (seedPassword == null || seedPassword.isBlank()) {
                // Canh bao ngan gon, khong in mat khau
                log.warn("app.seed.password chua duoc dat, bo qua viec tao du lieu mau");
                return;
            }

            seedAccount("admin@studymate.local", "Admin StudyMate", Role.ADMIN);
            seedAccount("manager@studymate.local", "Manager StudyMate", Role.MANAGER);
            seedAccount("student@studymate.local", "Student StudyMate", Role.STUDENT);
        };
    }

    private void seedAccount(String email, String fullName, Role role) {
        if (userRepository.existsByEmail(email)) {
            return; // Da ton tai, bo qua
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(seedPassword));
        user.setFullName(fullName);
        user.setRole(role);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Da tao tai khoan mau: {}", email);
    }
}
