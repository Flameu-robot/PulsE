package com.example.feedservice.service;

import com.example.feedservice.dto.response.BookmarkResponse;
import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.dto.response.PostResponse;
import com.example.feedservice.entity.Bookmark;
import com.example.feedservice.repository.BookmarkRepository;
import com.example.feedservice.repository.PostLikeRepository;
import exception.feed.DuplicateBookmarkException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final PostLikeRepository likeRepository;
    private final PostService postService;

    @Transactional
    public BookmarkResponse addBookmark(Long userId, Long postId) {
        if (bookmarkRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new DuplicateBookmarkException(postId, userId);
        }

        var post = postService.getPostOrThrow(postId);

        var bookmark = Bookmark.builder()
                .userId(userId)
                .post(post)
                .build();

        Bookmark saved = bookmarkRepository.save(bookmark);

        log.debug("Bookmark added: user={}, post={}", userId, postId);
        return new BookmarkResponse(saved.getId(), postId, saved.getCreatedAt());
    }

    @Transactional
    public void removeBookmark(Long userId, Long postId) {
        bookmarkRepository.deleteByUserIdAndPostId(userId, postId);
        log.debug("Bookmark removed: user={}, post={}", userId, postId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getBookmarkedPosts(Long userId, Pageable pageable) {
        var page = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(bookmark -> {
                    var post = bookmark.getPost();
                    boolean liked = likeRepository.existsByPostIdAndUserId(post.getId(), userId);
                    return postService.enrichWithUserContext(post, userId);
                });

        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(Long userId, Long postId) {
        return bookmarkRepository.existsByUserIdAndPostId(userId, postId);
    }
}
