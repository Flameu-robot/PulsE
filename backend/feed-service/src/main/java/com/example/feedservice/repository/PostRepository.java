package com.example.feedservice.repository;

import com.example.feedservice.entity.Post;
import com.example.feedservice.entity.enums.PostVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {


    @Query("""
            SELECT DISTINCT p FROM Post p
            LEFT JOIN FETCH p.attachments
            LEFT JOIN FETCH p.musicLinks
            LEFT JOIN FETCH p.stats
            WHERE p.id = :id
            """)
    Optional<Post> findByIdWithDetails(@Param("id") Long id);

    Page<Post> findByAuthorIdAndVisibilityInOrderByCreatedAtDesc(
            Long authorId,
            Collection<PostVisibility> visibilities,
            Pageable pageable
    );

    @Query("""
            SELECT p FROM Post p
            WHERE p.authorId IN :authorIds
              AND p.visibility IN :visibilities
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findFeedByAuthors(
            @Param("authorIds") Collection<Long> authorIds,
            @Param("visibilities") Collection<PostVisibility> visibilities,
            Pageable pageable
    );

    @Query("""
            SELECT p FROM Post p
            WHERE p.visibility = 'PUBLIC'
              AND p.createdAt > :since
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findPublicPostsSince(
            @Param("since") Instant since,
            Pageable pageable
    );

    List<Post> findByAuthorIdAndPinnedTrueOrderByCreatedAtDesc(Long authorId);

    long countByAuthorId(Long authorId);

    @Query("""
            SELECT DISTINCT p FROM Post p
            LEFT JOIN FETCH p.attachments
            LEFT JOIN FETCH p.stats
            WHERE p.id IN :ids
            """)
    List<Post> findAllByIdWithDetails(@Param("ids") Collection<Long> ids);

    @Modifying
    @Query("DELETE FROM Post p WHERE p.authorId = :authorId")
    int deleteAllByAuthorId(@Param("authorId") Long authorId);

    @Query("""
            SELECT p FROM Post p
            INNER JOIN p.postGroups pg
            WHERE pg.groupId = :groupId
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Post p
            INNER JOIN p.postGroups pg
            WHERE pg.groupId IN :groupIds
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findByGroupIds(@Param("groupIds") Collection<Long> groupIds, Pageable pageable);
}
