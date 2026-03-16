package com.example.feedservice.service;

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
        Instant since = Instant.now().minus(7, ChronoUnit.DAYS);
        Page<Post> posts = postRepository.findPublicPostsSince(since, pageable);

        return PagedResponse.from(enrichPage(posts, currentUserId));
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
}
