package com.studymate.user.repository;

import com.studymate.user.dto.AdminUserDto;
import com.studymate.user.entity.Role;
import com.studymate.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
            SELECT new com.studymate.user.dto.AdminUserDto(
                u.id, u.email, u.fullName, u.role, u.enabled, u.createdAt
            )
            FROM User u
            WHERE (:keyword IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:role IS NULL OR u.role = :role)
              AND (:enabled IS NULL OR u.enabled = :enabled)
            """)
    Page<AdminUserDto> searchUsers(@Param("keyword") String keyword,
                                   @Param("role") Role role,
                                   @Param("enabled") Boolean enabled,
                                   Pageable pageable);
}
