package com.studymate.user.service;

import com.studymate.user.dto.AvailabilitySlotDto;
import com.studymate.user.dto.ProfileUpdateRequest;
import com.studymate.user.dto.ProfileViewDto;
import com.studymate.user.dto.SubjectDto;
import com.studymate.user.entity.AvailabilitySlot;
import com.studymate.user.entity.ProfileSubject;
import com.studymate.user.entity.StudentProfile;
import com.studymate.user.entity.User;
import com.studymate.user.repository.StudentProfileRepository;
import com.studymate.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class ProfileService {

    private final StudentProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(StudentProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ProfileViewDto getProfileForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        return profileRepository.findWithDetailsByUserId(userId)
                .map(this::toViewDto)
                .orElseGet(() -> emptyViewDto(user));
    }

    // Hop dong cho module Matching va Insight doc du lieu ho so theo danh sach userId
    @Transactional(readOnly = true)
    public List<ProfileViewDto> getProfilesByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return profileRepository.findAllWithDetailsByUserIdIn(userIds)
                .stream()
                .map(this::toViewDto)
                .toList();
    }

    // Hop dong cho module Matching loc ung vien theo khoa va mon hoc
    @Transactional(readOnly = true)
    public List<ProfileViewDto> searchCandidateProfiles(Long excludeUserId, String faculty, String subjectKeyword) {
        String normalizedFaculty = blankToNull(faculty);
        String normalizedSubject = blankToNull(subjectKeyword);
        return profileRepository.findCandidateProfiles(excludeUserId, normalizedFaculty, normalizedSubject)
                .stream()
                .map(this::toViewDto)
                .toList();
    }

    @Transactional
    public ProfileViewDto updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        user.setFullName(request.getFullName().trim());
        userRepository.save(user);

        StudentProfile profile = profileRepository.findWithDetailsByUserId(userId)
                .orElseGet(() -> {
                    StudentProfile created = new StudentProfile();
                    created.setUser(user);
                    return created;
                });

        profile.setFaculty(blankToNull(request.getFaculty()));
        profile.setClassName(blankToNull(request.getClassName()));
        profile.setAcademicLevel(request.getAcademicLevel());
        profile.setStudyGoal(request.getStudyGoal());
        profile.setBio(blankToNull(request.getBio()));
        profile.setUpdatedAt(LocalDateTime.now());

        // Xoa va flush truoc khi chen lai de tranh vi pham UNIQUE (profile_id, subject_code) do Hibernate INSERT truoc DELETE
        profile.getSubjects().clear();
        profile.getAvailabilitySlots().clear();
        if (profile.getId() != null) {
            profileRepository.flush();
        }

        addSubjects(profile, request.getSubjects());
        addAvailabilitySlots(profile, request.getAvailabilitySlots());

        StudentProfile saved = profileRepository.save(profile);
        return toViewDto(saved);
    }

    private void addSubjects(StudentProfile profile, List<SubjectDto> incoming) {
        if (incoming == null) {
            return;
        }
        // Loai bo mon trung ma (khong phan biet hoa thuong) truoc khi luu
        Map<String, SubjectDto> uniqueSubjects = new LinkedHashMap<>();
        for (SubjectDto dto : incoming) {
            if (dto == null) {
                continue;
            }
            String code = blankToNull(dto.getSubjectCode());
            String name = blankToNull(dto.getSubjectName());
            if (code == null && name == null) {
                continue;
            }
            if (code == null || name == null) {
                throw new IllegalArgumentException("Vui lòng nhập đầy đủ mã môn và tên môn");
            }
            String normalizedCode = code.toUpperCase(Locale.ROOT);
            uniqueSubjects.putIfAbsent(normalizedCode, new SubjectDto(normalizedCode, name));
        }

        for (SubjectDto dto : uniqueSubjects.values()) {
            ProfileSubject subject = new ProfileSubject();
            subject.setProfile(profile);
            subject.setSubjectCode(dto.getSubjectCode());
            subject.setSubjectName(dto.getSubjectName());
            profile.getSubjects().add(subject);
        }
    }

    private void addAvailabilitySlots(StudentProfile profile, List<AvailabilitySlotDto> incoming) {
        if (incoming == null) {
            return;
        }
        for (AvailabilitySlotDto dto : incoming) {
            if (dto == null) {
                continue;
            }
            if (dto.getDayOfWeek() == null && dto.getStartTime() == null && dto.getEndTime() == null) {
                continue;
            }
            if (dto.getDayOfWeek() == null || dto.getStartTime() == null || dto.getEndTime() == null) {
                throw new IllegalArgumentException("Vui lòng chọn đầy đủ thứ và khung giờ rảnh");
            }
            if (!dto.getStartTime().isBefore(dto.getEndTime())) {
                throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu");
            }
            boolean duplicated = profile.getAvailabilitySlots().stream().anyMatch(existing ->
                    Objects.equals(existing.getDayOfWeek(), dto.getDayOfWeek())
                            && Objects.equals(existing.getStartTime(), dto.getStartTime())
                            && Objects.equals(existing.getEndTime(), dto.getEndTime()));
            if (duplicated) {
                continue;
            }
            AvailabilitySlot slot = new AvailabilitySlot();
            slot.setProfile(profile);
            slot.setDayOfWeek(dto.getDayOfWeek());
            slot.setStartTime(dto.getStartTime());
            slot.setEndTime(dto.getEndTime());
            profile.getAvailabilitySlots().add(slot);
        }
    }

    private ProfileViewDto toViewDto(StudentProfile profile) {
        User user = profile.getUser();
        List<SubjectDto> subjects = profile.getSubjects().stream()
                .map(s -> new SubjectDto(s.getSubjectCode(), s.getSubjectName()))
                .toList();
        List<AvailabilitySlotDto> slots = profile.getAvailabilitySlots().stream()
                .map(slot -> new AvailabilitySlotDto(slot.getDayOfWeek(), slot.getStartTime(), slot.getEndTime()))
                .toList();

        return new ProfileViewDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                profile.getFaculty(),
                profile.getClassName(),
                profile.getAcademicLevel(),
                profile.getStudyGoal(),
                profile.getBio(),
                subjects,
                slots,
                profile.getUpdatedAt()
        );
    }

    private ProfileViewDto emptyViewDto(User user) {
        return new ProfileViewDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                null
        );
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
