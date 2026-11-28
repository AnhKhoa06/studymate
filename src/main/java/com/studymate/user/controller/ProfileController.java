package com.studymate.user.controller;

import com.studymate.common.security.SecurityUtils;
import com.studymate.user.dto.AvailabilitySlotDto;
import com.studymate.user.dto.ProfileUpdateRequest;
import com.studymate.user.dto.ProfileViewDto;
import com.studymate.user.dto.SubjectDto;
import com.studymate.user.entity.AcademicLevel;
import com.studymate.user.entity.StudyGoal;
import com.studymate.user.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final Map<Integer, String> DAYS_OF_WEEK = createDaysOfWeek();

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public String viewProfile(Model model) {
        Long userId = requireCurrentUserId();
        ProfileViewDto profile = profileService.getProfileForUser(userId);

        model.addAttribute("profile", profile);
        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", toForm(profile));
        }
        addReferenceData(model);
        return "user/profile";
    }

    @PostMapping
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileUpdateRequest form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long userId = requireCurrentUserId();

        if (!bindingResult.hasErrors()) {
            try {
                profileService.updateProfile(userId, form);
                redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật hồ sơ học tập.");
                return "redirect:/profile";
            } catch (IllegalArgumentException ex) {
                bindingResult.reject("profile.invalid", ex.getMessage());
            }
        }

        ProfileViewDto profile = profileService.getProfileForUser(userId);
        model.addAttribute("profile", profile);
        addReferenceData(model);
        return "user/profile";
    }

    private Long requireCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Chưa đăng nhập"));
    }

    private ProfileUpdateRequest toForm(ProfileViewDto profile) {
        ProfileUpdateRequest form = new ProfileUpdateRequest();
        form.setFullName(profile.fullName());
        form.setFaculty(profile.faculty());
        form.setClassName(profile.className());
        form.setAcademicLevel(profile.academicLevel());
        form.setStudyGoal(profile.studyGoal());
        form.setBio(profile.bio());
        form.setSubjects(new ArrayList<>(profile.subjects().stream()
                .map(s -> new SubjectDto(s.getSubjectCode(), s.getSubjectName()))
                .toList()));
        form.setAvailabilitySlots(new ArrayList<>(profile.availabilitySlots().stream()
                .map(a -> new AvailabilitySlotDto(a.getDayOfWeek(), a.getStartTime(), a.getEndTime()))
                .toList()));
        return form;
    }

    private void addReferenceData(Model model) {
        model.addAttribute("academicLevels", AcademicLevel.values());
        model.addAttribute("studyGoals", StudyGoal.values());
        model.addAttribute("daysOfWeek", DAYS_OF_WEEK);
    }

    private static Map<Integer, String> createDaysOfWeek() {
        Map<Integer, String> map = new LinkedHashMap<>();
        map.put(1, "Thứ 2");
        map.put(2, "Thứ 3");
        map.put(3, "Thứ 4");
        map.put(4, "Thứ 5");
        map.put(5, "Thứ 6");
        map.put(6, "Thứ 7");
        map.put(7, "Chủ nhật");
        return map;
    }
}
