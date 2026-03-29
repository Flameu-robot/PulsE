package com.example.feedservice.repository;

import com.example.feedservice.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    @Query("""
            SELECT b FROM Bookmark b
            WHERE b.userId = :userId
            ORDER BY b.createdAt DESC
            """)
    Page<Bookmark> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT b.post.id FROM Bookmark b
            WHERE b.userId = :userId
              AND b.post.id IN :postIds
            """)
    List<Long> findBookmarkedPostIds(
            @Param("userId") Long userId,
            @Param("postIds") Collection<Long> postIds
    );

    void deleteByUserIdAndPostId(Long userId, Long postId);

    long countByUserId(Long userId);
}
