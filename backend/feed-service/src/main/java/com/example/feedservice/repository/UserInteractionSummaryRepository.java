package com.example.feedservice.repository;

import com.example.feedservice.entity.UserInteractionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserInteractionSummaryRepository extends JpaRepository<UserInteractionSummary, Long> {

    Optional<UserInteractionSummary> findByUserIdAndAuthorId(Long userId, Long authorId);

    @Query("""
            SELECT s.authorId FROM UserInteractionSummary s
            WHERE s.userId = :userId
            ORDER BY s.score DESC
            LIMIT :limit
            """)
    List<Long> findTopAuthorIdsByUserId(
            @Param("userId") Long userId,
            @Param("limit") int limit
    );

    boolean existsByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM UserInteractionSummary s WHERE s.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
