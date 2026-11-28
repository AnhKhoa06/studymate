package com.studymate.user;

import com.studymate.user.entity.AcademicLevel;
import com.studymate.user.entity.AvailabilitySlot;
import com.studymate.user.entity.ProfileSubject;
import com.studymate.user.entity.Role;
import com.studymate.user.entity.StudentProfile;
import com.studymate.user.entity.StudyGoal;
import com.studymate.user.entity.User;
import com.studymate.user.repository.StudentProfileRepository;
import com.studymate.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProfileRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository profileRepository;

    @Test
    void findWithDetailsAndCandidates_returnsFilteredProfiles() {
        User u1 = createUser("sv1@studymate.test", "Sinh Vien 1");
        User u2 = createUser("sv2@studymate.test", "Sinh Vien 2");

        saveProfile(u1, "Công nghệ thông tin", "INT1434", "OOP", 1, LocalTime.of(8, 0), LocalTime.of(10, 0));
        saveProfile(u2, "An toàn thông tin", "INT1306", "Cấu trúc dữ liệu", 2, LocalTime.of(14, 0), LocalTime.of(16, 0));

        StudentProfile loaded = profileRepository.findWithDetailsByUserId(u1.getId()).orElseThrow();
        assertThat(loaded.getSubjects()).hasSize(1);
        assertThat(loaded.getAvailabilitySlots()).hasSize(1);

        List<StudentProfile> candidates = profileRepository.findCandidateProfiles(u1.getId(), "An toàn", null);
        assertThat(candidates).extracting(p -> p.getUser().getId()).containsExactly(u2.getId());
    }

    private User createUser(String email, String fullName) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash("hashed");
        u.setFullName(fullName);
        u.setRole(Role.STUDENT);
        u.setEnabled(true);
        u.setCreatedAt(LocalDateTime.now());
        return userRepository.save(u);
    }

    private void saveProfile(User user, String faculty, String code, String name,
                             int day, LocalTime start, LocalTime end) {
        StudentProfile p = new StudentProfile();
        p.setUser(user);
        p.setFaculty(faculty);
        p.setClassName("D22CQCN01");
        p.setAcademicLevel(AcademicLevel.INTERMEDIATE);
        p.setStudyGoal(StudyGoal.PROJECT);
        p.setUpdatedAt(LocalDateTime.now());

        ProfileSubject sub = new ProfileSubject();
        sub.setProfile(p);
        sub.setSubjectCode(code);
        sub.setSubjectName(name);
        p.getSubjects().add(sub);

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setProfile(p);
        slot.setDayOfWeek(day);
        slot.setStartTime(start);
        slot.setEndTime(end);
        p.getAvailabilitySlots().add(slot);

        profileRepository.save(p);
    }
}
