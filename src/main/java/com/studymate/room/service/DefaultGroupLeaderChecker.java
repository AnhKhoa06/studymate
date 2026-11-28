package com.studymate.room.service;

import org.springframework.stereotype.Component;

// Ban cai dat mac dinh an toan (tu choi) cho den khi module group cung cap bean that (@Primary)
@Component
public class DefaultGroupLeaderChecker implements GroupLeaderChecker {

    @Override
    public boolean isGroupLeader(Long userId, Long groupId) {
        return false;
    }
}
