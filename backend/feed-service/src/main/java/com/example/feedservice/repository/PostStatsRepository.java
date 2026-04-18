package com.example.feedservice.repository;

import com.example.feedservice.entity.PostStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostStatsRepository extends JpaRepository<PostStats, Long> {

    @Modifying
    @Query("UPDATE PostStats s SET s.likesCount = s.likesCount + 1 WHERE s.postId = :postId")
    void incrementLikes(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostStats s SET s.likesCount = GREATEST(0, s.likesCount - 1) WHERE s.postId = :postId")
    void decrementLikes(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostStats s SET s.commentsCount = s.commentsCount + 1 WHERE s.postId = :postId")
    void incrementComments(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostStats s SET s.commentsCount = GREATEST(0, s.commentsCount - 1) WHERE s.postId = :postId")
    void decrementComments(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostStats s SET s.sharesCount = s.sharesCount + 1 WHERE s.postId = :postId")
    void incrementShares(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostStats s SET s.viewsCount = s.viewsCount + 1 WHERE s.postId = :postId")
    void incrementViews(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE PostStats s SET s.viewsCount = s.viewsCount + :count WHERE s.postId = :postId")
    void incrementViewsByCount(@Param("postId") Long postId, @Param("count") int count);
}
