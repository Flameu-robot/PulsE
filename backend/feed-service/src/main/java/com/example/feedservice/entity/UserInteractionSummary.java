package com.example.feedservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "user_interaction_summary",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_interaction_user_author",
                columnNames = {"user_id", "author_id"}
        ),
        indexes = {
                @Index(name = "idx_interaction_user_score", columnList = "user_id, score DESC"),
                @Index(name = "idx_interaction_updated", columnList = "updated_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInteractionSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private int likeCount = 0;

    @Column(name = "bookmark_count", nullable = false)
    @Builder.Default
    private int bookmarkCount = 0;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @Column(name = "score", nullable = false)
    @Builder.Default
    private double score = 0.0;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    public void incrementLikes() {
        likeCount++;
        recalculateScore();
    }

    public void decrementLikes() {
        likeCount = Math.max(0, likeCount - 1);
        recalculateScore();
    }

    public void incrementBookmarks() {
        bookmarkCount++;
        recalculateScore();
    }

    public void decrementBookmarks() {
        bookmarkCount = Math.max(0, bookmarkCount - 1);
        recalculateScore();
    }

    public void incrementViews() {
        viewCount++;
        recalculateScore();
    }

    private void recalculateScore() {
        this.score = likeCount * 3.0 + bookmarkCount * 5.0 + viewCount * 1.0;
        this.updatedAt = Instant.now();
    }
}