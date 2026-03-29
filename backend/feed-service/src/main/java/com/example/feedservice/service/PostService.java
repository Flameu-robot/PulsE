package com.example.feedservice.service;

import com.example.feedservice.client.MessagingServiceClient;
import com.example.feedservice.dto.request.CreatePostRequest;
import com.example.feedservice.dto.request.UpdatePostRequest;
import com.example.feedservice.dto.response.*;
import com.example.feedservice.entity.Post;
import com.example.feedservice.entity.PostAttachment;
import com.example.feedservice.entity.PostMusicLink;
import com.example.feedservice.entity.PostStats;
import com.example.feedservice.entity.enums.FollowStatus;
import com.example.feedservice.entity.enums.PostVisibility;
import com.example.feedservice.repository.*;
import exception.feed.GroupPermissionDeniedException;
import exception.feed.PostAccessDeniedException;
import exception.feed.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostStatsRepository statsRepository;
    private final PostLikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final FollowRepository followRepository;
    private final MessagingServiceClient messagingServiceClient;

    @Transactional
    public PostResponse createPost(Long authorId, CreatePostRequest request) {
        var post = Post.builder()
                .authorId(authorId)
                .content(request.content())
                .visibility(request.visibility())
                .build();

        if (!request.attachments().isEmpty()) {
            request.attachments().forEach(req -> {
                var attachment = PostAttachment.builder()
                        .mediaType(req.mediaType())
                        .url(req.url())
                        .previewUrl(req.previewUrl())
                        .orderIndex(req.orderIndex())
                        .metadata(req.metadata())
                        .build();
                post.addAttachment(attachment);
            });
        }

        if (!request.trackIds().isEmpty()) {
            request.trackIds().forEach(trackId -> {
                var link = PostMusicLink.builder().trackId(trackId).build();
                post.addMusicLink(link);
            });
        }

        if (!request.groupIds().isEmpty()) {
            publishToGroups(post, authorId, request.groupIds());
        }

        Post saved = postRepository.save(post);

        var stats = PostStats.builder().post(saved).build();
        statsRepository.save(stats);
        saved.setStats(stats);

        log.info("Post created: id={}, author={}, groups={}",
                saved.getId(), authorId, saved.getGroupIds());

        return toPostResponse(saved, false, false);
    }

    @Transactional
    public PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request) {
        Post post = getPostOrThrow(postId);
        checkOwnership(post, userId);

        if (request.content() != null) {
            post.setContent(request.content());
        }
        if (request.visibility() != null) {
            post.setVisibility(request.visibility());
        }
        if (request.pinned() != null) {
            post.setPinned(request.pinned());
        }

        Post saved = postRepository.save(post);
        log.info("Post updated: id={}", postId);

        return toPostResponse(
                saved,
                likeRepository.existsByPostIdAndUserId(postId, userId),
                bookmarkRepository.existsByUserIdAndPostId(userId, postId)
        );
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = getPostOrThrow(postId);
        checkOwnership(post, userId);

        postRepository.delete(post);
        log.info("Post deleted: id={}, author={}", postId, userId);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long postId, Long currentUserId) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        boolean liked = currentUserId != null
                && likeRepository.existsByPostIdAndUserId(postId, currentUserId);
        boolean bookmarked = currentUserId != null
                && bookmarkRepository.existsByUserIdAndPostId(currentUserId, postId);

        return toPostDetailResponse(post, liked, bookmarked);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getUserPosts(Long authorId, Long currentUserId, Pageable pageable) {
        var visibilities = resolveVisibilities(authorId, currentUserId);

        Page<PostResponse> page = postRepository
                .findByAuthorIdAndVisibilityInOrderByCreatedAtDesc(authorId, visibilities, pageable)
                .map(post -> enrichWithUserContext(post, currentUserId));

        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getGroupPosts(Long groupId, Long currentUserId, Pageable pageable) {
        Page<PostResponse> page = postRepository.findByGroupId(groupId, pageable)
                .map(post -> enrichWithUserContext(post, currentUserId));

        return PagedResponse.from(page);
    }

    Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    PostResponse enrichWithUserContext(Post post, Long currentUserId) {
        boolean liked = currentUserId != null
                && likeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);
        boolean bookmarked = currentUserId != null
                && bookmarkRepository.existsByUserIdAndPostId(currentUserId, post.getId());
        return toPostResponse(post, liked, bookmarked);
    }

    private void publishToGroups(Post post, Long authorId, List<Long> groupIds) {
        List<Long> allowedGroupIds = messagingServiceClient.checkPostPermissions(authorId, groupIds);

        if (allowedGroupIds.size() != groupIds.size()) {
            var deniedIds = new ArrayList<>(groupIds);
            deniedIds.removeAll(allowedGroupIds);
            throw new GroupPermissionDeniedException(deniedIds);
        }

        allowedGroupIds.forEach(post::addToGroup);
    }

    private void checkOwnership(Post post, Long userId) {
        if (!post.getAuthorId().equals(userId)) {
            throw new PostAccessDeniedException(post.getId());
        }
    }

    private Set<PostVisibility> resolveVisibilities(Long authorId, Long currentUserId) {
        if (currentUserId == null) {
            return EnumSet.of(PostVisibility.PUBLIC);
        }

        if (authorId.equals(currentUserId)) {
            return EnumSet.allOf(PostVisibility.class);
        }

        var v = EnumSet.of(PostVisibility.PUBLIC);
        if (followRepository.existsByFollowerIdAndFolloweeIdAndStatus(
                currentUserId, authorId, FollowStatus.ACTIVE)) {
            v.add(PostVisibility.FRIENDS);
        }

        return v;
    }

    private PostResponse toPostResponse(Post post, boolean likedByMe, boolean bookmarkedByMe) {
        return new PostResponse(
                post.getId(),
                post.getAuthorId(),
                post.getContent(),
                post.getVisibility(),
                post.isPinned(),
                mapAttachments(post.getAttachments()),
                mapTrackIds(post.getMusicLinks()),
                post.getGroupIds(),
                mapStats(post.getStats()),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                likedByMe,
                bookmarkedByMe
        );
    }

    private PostDetailResponse toPostDetailResponse(Post post, boolean likedByMe, boolean bookmarkedByMe) {
        return new PostDetailResponse(
                post.getId(),
                post.getAuthorId(),
                post.getContent(),
                post.getVisibility(),
                post.isPinned(),
                post.getMetadata(),
                mapAttachments(post.getAttachments()),
                mapMusicLinks(post.getMusicLinks()),
                post.getGroupIds(),
                mapStats(post.getStats()),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                likedByMe,
                bookmarkedByMe
        );
    }

    private List<AttachmentResponse> mapAttachments(Set<PostAttachment> attachments) {
        if (attachments == null) return List.of();
        return attachments.stream()
                .sorted(Comparator.comparingInt(PostAttachment::getOrderIndex))
                .map(a -> new AttachmentResponse(
                        a.getId(), a.getMediaType(), a.getUrl(),
                        a.getPreviewUrl(), a.getOrderIndex(), a.getMetadata()))
                .toList();
    }

    private List<Long> mapTrackIds(Set<PostMusicLink> links) {
        if (links == null) return List.of();
        return links.stream().map(PostMusicLink::getTrackId).toList();
    }

    private List<MusicLinkResponse> mapMusicLinks(Set<PostMusicLink> links) {
        if (links == null) return List.of();
        return links.stream()
                .map(l -> new MusicLinkResponse(l.getId(), l.getTrackId(), l.getListenCount()))
                .toList();
    }

    private PostStatsResponse mapStats(PostStats stats) {
        if (stats == null) return new PostStatsResponse(0, 0, 0, 0);
        return new PostStatsResponse(
                stats.getLikesCount(), stats.getCommentsCount(),
                stats.getSharesCount(), stats.getViewsCount());
    }
}
