package com.example.feedservice.repository;

import com.example.feedservice.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(
            Long postId,
            Pageable pageable
    );

    Page<Comment> findByParentCommentIdOrderByCreatedAtAsc(
            Long parentCommentId,
            Pageable pageable
    );

    Page<Comment> findByPostIdOrderByCreatedAtAsc(Long postId, Pageable pageable);

    long countByPostIdAndParentCommentIsNull(Long postId);

    long countByParentCommentId(Long parentCommentId);

    @Query("""
            SELECT c FROM Comment c
            WHERE c.authorId = :authorId
            ORDER BY c.createdAt DESC
            """)
    Page<Comment> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);
}
