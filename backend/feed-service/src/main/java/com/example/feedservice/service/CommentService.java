package com.example.feedservice.service;

import com.example.feedservice.dto.request.CreateCommentRequest;
import com.example.feedservice.dto.response.CommentResponse;
import com.example.feedservice.dto.response.PagedResponse;
import com.example.feedservice.entity.Comment;
import com.example.feedservice.repository.CommentRepository;
import com.example.feedservice.repository.PostStatsRepository;
import exception.feed.CommentNotFoundException;
import exception.feed.PostAccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostStatsRepository statsRepository;
    private final PostService postService;

    @Transactional
    public CommentResponse addComment(Long postId, Long authorId, CreateCommentRequest request) {
        var post = postService.getPostOrThrow(postId);

        var comment = Comment.builder()
                .post(post)
                .authorId(authorId)
                .content(request.content())
                .build();

        if (request.parentCommentId() != null) {
            Comment parent = commentRepository.findById(request.parentCommentId())
                    .orElseThrow(() -> new CommentNotFoundException(request.parentCommentId()));
            comment.setParentComment(parent);
        }

        Comment saved = commentRepository.save(comment);
        statsRepository.incrementComments(postId);

        log.debug("Comment created: id={}, post={}, author={}", saved.getId(), postId, authorId);
        return toResponse(saved, 0);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        boolean isCommentAuthor = comment.getAuthorId().equals(userId);
        boolean isPostAuthor = comment.getPost().getAuthorId().equals(userId);

        if (!isCommentAuthor && !isPostAuthor) {
            throw new PostAccessDeniedException("No permission to delete this comment");
        }

        Long postId = comment.getPost().getId();
        commentRepository.delete(comment);
        statsRepository.decrementComments(postId);

        log.debug("Comment deleted: id={}", commentId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> getRootComments(Long postId, Pageable pageable) {
        var page = commentRepository
                .findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(postId, pageable)
                .map(comment -> toResponse(comment,
                        commentRepository.countByParentCommentId(comment.getId())));

        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> getReplies(Long parentCommentId, Pageable pageable) {
        var page = commentRepository
                .findByParentCommentIdOrderByCreatedAtAsc(parentCommentId, pageable)
                .map(comment -> toResponse(comment,
                        commentRepository.countByParentCommentId(comment.getId())));

        return PagedResponse.from(page);
    }

    private CommentResponse toResponse(Comment comment, long replyCount) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getAuthorId(),
                comment.getContent(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                replyCount,
                comment.getCreatedAt());
    }
}