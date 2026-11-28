package com.studymate.user.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 100)
    private String faculty;

    @Column(name = "class_name", length = 50)
    private String className;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_level", length = 50)
    private AcademicLevel academicLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "study_goal", length = 50)
    private StudyGoal studyGoal;

    @Column(length = 500)
    private String bio;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Dung Set de EntityGraph tai dong thoi ca subjects va availabilitySlots khong bi MultipleBagFetchException
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("subjectCode ASC")
    private Set<ProfileSubject> subjects = new LinkedHashSet<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayOfWeek ASC, startTime ASC")
    private Set<AvailabilitySlot> availabilitySlots = new LinkedHashSet<>();
}
