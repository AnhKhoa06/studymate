package com.studymate.user.repository;

import com.studymate.user.entity.StudentProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    @EntityGraph(attributePaths = {"user", "subjects", "availabilitySlots"})
    Optional<StudentProfile> findWithDetailsByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "subjects", "availabilitySlots"})
    List<StudentProfile> findAllWithDetailsByUserIdIn(Collection<Long> userIds);

    // Loc ung vien cho module matching theo khoa va mon hoc, loai tru nguoi dung hien tai
    @EntityGraph(attributePaths = {"user", "subjects", "availabilitySlots"})
    @Query("""
            SELECT DISTINCT p FROM StudentProfile p
            JOIN p.user u
            LEFT JOIN p.subjects s
            WHERE u.enabled = true
              AND u.role = com.studymate.user.entity.Role.STUDENT
              AND (:excludeUserId IS NULL OR u.id <> :excludeUserId)
              AND (:faculty IS NULL OR LOWER(p.faculty) LIKE LOWER(CONCAT('%', :faculty, '%')))
              AND (:subjectKeyword IS NULL OR LOWER(s.subjectCode) LIKE LOWER(CONCAT('%', :subjectKeyword, '%'))
                   OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :subjectKeyword, '%')))
            """)
    List<StudentProfile> findCandidateProfiles(@Param("excludeUserId") Long excludeUserId,
                                               @Param("faculty") String faculty,
                                               @Param("subjectKeyword") String subjectKeyword);
}
