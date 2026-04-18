package com.example.feedservice.service;

import com.example.feedservice.entity.UserInteractionSummary;
import com.example.feedservice.repository.UserInteractionSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionService {

    private final UserInteractionSummaryRepository summaryRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLike(Long userId, Long authorId) {
        getOrCreate(userId, authorId).incrementLikes();
        log.debug("Interaction like: user={} → author={}", userId, authorId);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUnlike(Long userId, Long authorId) {
        summaryRepository.findByUserIdAndAuthorId(userId, authorId)
                .ifPresent(s -> {
                    s.decrementLikes();
                    log.debug("Interaction unlike: user={} → author={}", userId, authorId);
                });
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onBookmark(Long userId, Long authorId) {
        getOrCreate(userId, authorId).incrementBookmarks();
        log.debug("Interaction bookmark: user={} → author={}", userId, authorId);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUnbookmark(Long userId, Long authorId) {
        summaryRepository.findByUserIdAndAuthorId(userId, authorId)
                .ifPresent(s -> {
                    s.decrementBookmarks();
                    log.debug("Interaction unbookmark: user={} → author={}", userId, authorId);
                });
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onView(Long userId, Long authorId) {
        summaryRepository.findByUserIdAndAuthorId(userId, authorId)
                .ifPresent(UserInteractionSummary::incrementViews);
    }

    private UserInteractionSummary getOrCreate(Long userId, Long authorId) {
        return summaryRepository.findByUserIdAndAuthorId(userId, authorId)
                .orElseGet(() -> summaryRepository.save(
                        UserInteractionSummary.builder()
                                .userId(userId)
                                .authorId(authorId)
                                .build()
                ));
    }
}
