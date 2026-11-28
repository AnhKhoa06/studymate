package com.studymate.user.repository;

import com.studymate.user.entity.AiRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AiRequestRepository extends JpaRepository<AiRequest, Long> {

    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime after);

    List<AiRequest> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}
