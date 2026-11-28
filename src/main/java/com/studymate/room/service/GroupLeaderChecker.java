package com.studymate.room.service;

// Giao dien mong tach phu thuoc sang module group (Huy).
// Khi GroupService duoc merge, chi can viet lop cai dat interface nay.
public interface GroupLeaderChecker {

    boolean isGroupLeader(Long userId, Long groupId);
}
