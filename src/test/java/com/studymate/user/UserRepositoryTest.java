package com.studymate.user;

import com.studymate.user.dto.AdminUserDto;
import com.studymate.user.entity.Role;
import com.studymate.user.entity.User;
import com.studymate.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void searchUsers_filtersByKeywordRoleAndEnabled() {
        createUser("an@studymate.test", "Nguyen Van An", Role.STUDENT, true);
        createUser("binh@studymate.test", "Tran Thi Binh", Role.MANAGER, false);
        createUser("cuong@studymate.test", "Le Van Cuong", Role.ADMIN, true);

        Page<AdminUserDto> byKeyword = userRepository.searchUsers("binh", null, null, PageRequest.of(0, 10));
        assertThat(byKeyword.getContent()).extracting(AdminUserDto::email).containsExactly("binh@studymate.test");

        Page<AdminUserDto> byRoleAndEnabled = userRepository.searchUsers(null, Role.STUDENT, true, PageRequest.of(0, 10));
        assertThat(byRoleAndEnabled.getContent()).extracting(AdminUserDto::email).containsExactly("an@studymate.test");
    }

    private void createUser(String email, String fullName, Role role, boolean enabled) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash("hashed");
        u.setFullName(fullName);
        u.setRole(role);
        u.setEnabled(enabled);
        u.setCreatedAt(LocalDateTime.now());
        userRepository.save(u);
    }
}
