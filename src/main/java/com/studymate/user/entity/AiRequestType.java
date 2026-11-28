package com.studymate.user.entity;

public enum AiRequestType {
    STUDY_QA("Hỏi đáp học tập"),
    MATCH_EXPLAIN("Giải thích ghép đôi"),
    CHAT_CHECKLIST("Tóm tắt chat thành checklist");

    private final String label;

    AiRequestType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
