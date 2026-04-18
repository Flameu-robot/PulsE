package com.example.feedservice.repository;

import com.example.feedservice.entity.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    List<PostLike> findByPostIdInAndUserId(Collection<Long> postIds, Long userId);

    Page<PostLike> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

    void deleteByPostIdAndUserId(Long postId, Long userId);
}
