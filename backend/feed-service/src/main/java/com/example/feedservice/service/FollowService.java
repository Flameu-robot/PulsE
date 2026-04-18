package com.example.feedservice.service;

import com.example.feedservice.dto.response.FollowResponse;
import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.entity.Follow;
import com.example.feedservice.entity.enums.FollowStatus;
import com.example.feedservice.repository.FollowRepository;
import exception.feed.DuplicateFollowException;
import exception.feed.FollowNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;

    @Transactional
    public FollowResponse follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw DuplicateFollowException.selfFollow();
        }

        followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
                .ifPresent(existing -> {
                    throw new DuplicateFollowException(followerId, followeeId);
                });

        var follow = Follow.builder()
                .followerId(followerId)
                .followeeId(followeeId)
                .status(FollowStatus.ACTIVE)
                .build();

        Follow saved = followRepository.save(follow);
        log.info("Follow: {} → {}", followerId, followeeId);
        return toResponse(saved);
    }

    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        Follow follow = followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
                .orElseThrow(() -> new FollowNotFoundException(followerId, followeeId));

        followRepository.delete(follow);
        log.info("Unfollow: {} → {}", followerId, followeeId);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followeeId) {
        return followRepository.existsByFollowerIdAndFolloweeIdAndStatus(
                followerId, followeeId, FollowStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public PagedResponse<FollowResponse> getFollowers(Long userId, Pageable pageable) {
        var page = followRepository.findByFolloweeIdAndStatus(userId, FollowStatus.ACTIVE, pageable)
                .map(this::toResponse);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<FollowResponse> getFollowing(Long userId, Pageable pageable) {
        var page = followRepository.findByFollowerIdAndStatus(userId, FollowStatus.ACTIVE, pageable)
                .map(this::toResponse);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public long getFollowersCount(Long userId) {
        return followRepository.countByFolloweeIdAndStatus(userId, FollowStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        return followRepository.countByFollowerIdAndStatus(userId, FollowStatus.ACTIVE);
    }

    private FollowResponse toResponse(Follow follow) {
        return new FollowResponse(
                follow.getId(),
                follow.getFollowerId(),
                follow.getFolloweeId(),
                follow.getStatus(),
                follow.getCreatedAt());
    }
}