package com.example.feedservice.repository;

import com.example.feedservice.entity.Follow;
import com.example.feedservice.entity.enums.FollowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    boolean existsByFollowerIdAndFolloweeIdAndStatus(Long followerId, Long followeeId, FollowStatus status);

    @Query("""
            SELECT f.followeeId FROM Follow f
            WHERE f.followerId = :userId
              AND f.status = 'ACTIVE'
            """)
    List<Long> findActiveFolloweeIds(@Param("userId") Long userId);


    @Query("""
            SELECT f.followerId FROM Follow f
            WHERE f.followeeId = :userId
              AND f.status = 'ACTIVE'
            """)
    List<Long> findActiveFollowerIds(@Param("userId") Long userId);

    Page<Follow> findByFollowerIdAndStatus(Long followerId, FollowStatus status, Pageable pageable);

    Page<Follow> findByFolloweeIdAndStatus(Long followeeId, FollowStatus status, Pageable pageable);

    long countByFollowerIdAndStatus(Long followerId, FollowStatus status);

    long countByFolloweeIdAndStatus(Long followeeId, FollowStatus status);

    void deleteAllByFollowerId(Long followerId);

    void deleteAllByFolloweeId(Long followeeId);
}
