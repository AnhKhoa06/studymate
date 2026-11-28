package com.studymate.user.entity;

public enum AcademicLevel {
    BEGINNER("Mới bắt đầu"),
    INTERMEDIATE("Trung bình - Khá"),
    ADVANCED("Nâng cao");

    private final String label;

    AcademicLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
