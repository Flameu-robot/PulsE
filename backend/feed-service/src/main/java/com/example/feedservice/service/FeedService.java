package com.example.feedservice.service;

import com.example.feedservice.client.MessagingServiceClient;
import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.dto.response.PostResponse;
import com.example.feedservice.entity.Post;
import com.example.feedservice.entity.enums.PostVisibility;
import com.example.feedservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final PostLikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final PostService postService;
    private final MessagingServiceClient messagingServiceClient;
    private final UserInteractionSummaryRepository interactionSummaryRepository;

    private static final int    PREFERRED_AUTHORS_LIMIT = 50;
    private static final int    EXPLORE_WINDOW_DAYS     = 14;
    private static final double PERSONALIZED_THRESHOLD  = 0.5;

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getFollowingFeed(Long userId, Pageable pageable) {
        List<Long> followeeIds = followRepository.findActiveFolloweeIds(userId);

        if (followeeIds.isEmpty()) {
            return PagedResponse.from(Page.empty(pageable));
        }

        var visibilities = Set.of(PostVisibility.PUBLIC, PostVisibility.FRIENDS);
        Page<Post> posts = postRepository.findFeedByAuthors(followeeIds, visibilities, pageable);

        return PagedResponse.from(enrichPage(posts, userId));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getExploreFeed(Long currentUserId, Pageable pageable) {
        Instant since = Instant.now().minus(EXPLORE_WINDOW_DAYS, ChronoUnit.DAYS);

        List<Long> followingIds = currentUserId != null
                ? followRepository.findActiveFolloweeIds(currentUserId)
                : List.of();

        if (currentUserId == null || !interactionSummaryRepository.existsByUserId(currentUserId)) {
            log.debug("Explore cold start for userId={}", currentUserId);
            Page<Post> trending = postRepository.findTrendingPublicPosts(
                    since,
                    followingIds.isEmpty() ? List.of(-1L) : followingIds,
                    pageable
            );
            return PagedResponse.from(enrichPage(trending, currentUserId));
        }

        List<Long> preferredAuthorIds = interactionSummaryRepository
                .findTopAuthorIdsByUserId(currentUserId, PREFERRED_AUTHORS_LIMIT);

        Page<Post> personalized = postRepository.findPersonalizedExplorePosts(
                preferredAuthorIds,
                followingIds.isEmpty() ? List.of(-1L) : followingIds,
                since,
                pageable
        );

        log.debug("Explore personalized: userId={}, preferredAuthors={}, found={}",
                currentUserId, preferredAuthorIds.size(), personalized.getTotalElements());

        if (isSufficientlyFilled(personalized, pageable)) {
            return PagedResponse.from(enrichPage(personalized, currentUserId));
        }

        log.debug("Explore fallback to trending for userId={}", currentUserId);
        Page<Post> trending = postRepository.findTrendingPublicPosts(
                since,
                buildExcludeList(followingIds, preferredAuthorIds),
                pageable
        );

        return PagedResponse.from(enrichPage(trending, currentUserId));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getGroupsFeed(Long userId, Pageable pageable) {
        List<Long> userGroupIds = messagingServiceClient.getUserGroupIds(userId);

        if (userGroupIds.isEmpty()) {
            return PagedResponse.from(Page.empty(pageable));
        }

        Page<Post> posts = postRepository.findByGroupIds(userGroupIds, pageable);
        return PagedResponse.from(enrichPage(posts, userId));
    }

    private Page<PostResponse> enrichPage(Page<Post> posts, Long userId) {
        if (posts.isEmpty() || userId == null) {
            return posts.map(p -> postService.enrichWithUserContext(p, null));
        }

        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();

        Set<Long> likedPostIds = likeRepository.findByPostIdInAndUserId(postIds, userId)
                .stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toSet());

        Set<Long> bookmarkedPostIds = Set.copyOf(
                bookmarkRepository.findBookmarkedPostIds(userId, postIds)
        );

        return posts.map(post -> postService.enrichWithUserContext(post, userId));
    }

    private boolean isSufficientlyFilled(Page<?> page, Pageable pageable) {
        int requested = pageable.getPageSize();
        int received  = page.getNumberOfElements();
        return received >= (int)(requested * PERSONALIZED_THRESHOLD);
    }

    private List<Long> buildExcludeList(List<Long> followingIds, List<Long> preferredAuthorIds) {
        var exclude = new ArrayList<>(followingIds);
        exclude.addAll(preferredAuthorIds);
        return exclude.isEmpty() ? List.of(-1L) : exclude;
    }
}
