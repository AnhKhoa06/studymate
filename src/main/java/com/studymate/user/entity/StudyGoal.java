package com.studymate.user.entity;

public enum StudyGoal {
    EXAM_PREP("Ôn thi cuối kỳ"),
    PROJECT("Làm đồ án nhóm"),
    HOMEWORK("Giải bài tập cùng nhau"),
    RESEARCH("Nghiên cứu chuyên sâu"),
    SKILL_UP("Rèn kỹ năng thực hành");

    private final String label;

    StudyGoal(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
