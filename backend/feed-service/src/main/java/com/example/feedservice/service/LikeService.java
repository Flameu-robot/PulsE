package com.example.feedservice.service;

import com.example.feedservice.entity.PostLike;
import com.example.feedservice.repository.PostLikeRepository;
import com.example.feedservice.repository.PostStatsRepository;
import exception.feed.DuplicateLikeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostLikeRepository likeRepository;
    private final PostStatsRepository statsRepository;
    private final PostService postService;

    @Transactional
    public void likePost(Long postId, Long userId) {
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new DuplicateLikeException(postId, userId);
        }

        var post = postService.getPostOrThrow(postId);

        var like = PostLike.builder()
                .post(post)
                .userId(userId)
                .build();

        likeRepository.save(like);
        statsRepository.incrementLikes(postId);

        log.debug("Like: user={} → post={}", userId, postId);
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        likeRepository.findByPostIdAndUserId(postId, userId)
                .ifPresent(like -> {
                    likeRepository.delete(like);
                    statsRepository.decrementLikes(postId);
                    log.debug("Unlike: user={} → post={}", userId, postId);
                });
    }

    @Transactional(readOnly = true)
    public boolean isLiked(Long postId, Long userId) {
        return likeRepository.existsByPostIdAndUserId(postId, userId);
    }
}
