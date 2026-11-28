package com.studymate.user;

import com.studymate.common.security.CurrentUser;
import com.studymate.user.dto.ProfileViewDto;
import com.studymate.user.entity.AcademicLevel;
import com.studymate.user.entity.Role;
import com.studymate.user.entity.StudyGoal;
import com.studymate.user.entity.User;
import com.studymate.user.repository.StudentProfileRepository;
import com.studymate.user.repository.UserRepository;
import com.studymate.user.service.ProfileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository profileRepository;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getProfile_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void getAndPostProfile_authenticated_savesSubjectsAndSlots() throws Exception {
        User savedUser = createStudent("profile.test@studymate.test", "Nguyen Van A");
        CurrentUser principal = toPrincipal(savedUser);

        mockMvc.perform(get("/profile").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("profile", "profileForm"));

        mockMvc.perform(post("/profile")
                        .with(user(principal))
                        .with(csrf())
                        .param("fullName", "Nguyen Van A Updated")
                        .param("faculty", "Công nghệ thông tin")
                        .param("className", "D22CQCN01")
                        .param("academicLevel", AcademicLevel.INTERMEDIATE.name())
                        .param("studyGoal", StudyGoal.PROJECT.name())
                        .param("bio", "Tìm bạn làm đồ án OOP")
                        .param("subjects[0].subjectCode", "int1434")
                        .param("subjects[0].subjectName", "Phát triển phần mềm hướng đối tượng")
                        .param("availabilitySlots[0].dayOfWeek", "2")
                        .param("availabilitySlots[0].startTime", "08:00")
                        .param("availabilitySlots[0].endTime", "10:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        ProfileViewDto updated = profileService.getProfileForUser(savedUser.getId());
        assertThat(updated.fullName()).isEqualTo("Nguyen Van A Updated");
        assertThat(updated.faculty()).isEqualTo("Công nghệ thông tin");
        assertThat(updated.subjects()).hasSize(1);
        assertThat(updated.subjects().get(0).getSubjectCode()).isEqualTo("INT1434");
        assertThat(updated.availabilitySlots()).hasSize(1);

        // Luu lan 2 voi cung ma mon de dam bao khong bi loi trung UNIQUE constraint
        mockMvc.perform(post("/profile")
                        .with(user(principal))
                        .with(csrf())
                        .param("fullName", "Nguyen Van A Updated")
                        .param("faculty", "Công nghệ thông tin")
                        .param("subjects[0].subjectCode", "INT1434")
                        .param("subjects[0].subjectName", "Phát triển phần mềm hướng đối tượng")
                        .param("availabilitySlots[0].dayOfWeek", "2")
                        .param("availabilitySlots[0].startTime", "08:00")
                        .param("availabilitySlots[0].endTime", "10:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void postProfile_invalidTimeRange_returnsFormWithErrors() throws Exception {
        User savedUser = createStudent("profile.invalid@studymate.test", "Nguyen Van B");
        CurrentUser principal = toPrincipal(savedUser);

        mockMvc.perform(post("/profile")
                        .with(user(principal))
                        .with(csrf())
                        .param("fullName", "Nguyen Van B")
                        .param("availabilitySlots[0].dayOfWeek", "3")
                        .param("availabilitySlots[0].startTime", "14:00")
                        .param("availabilitySlots[0].endTime", "12:00"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());
    }

    private User createStudent(String email, String fullName) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode("Secret123!"));
        u.setFullName(fullName);
        u.setRole(Role.STUDENT);
        u.setEnabled(true);
        u.setCreatedAt(LocalDateTime.now());
        return userRepository.save(u);
    }

    private CurrentUser toPrincipal(User u) {
        return new CurrentUser(
                u.getId(),
                u.getEmail(),
                u.getPasswordHash(),
                u.getFullName(),
                u.getRole(),
                u.isEnabled(),
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
        );
    }
}
