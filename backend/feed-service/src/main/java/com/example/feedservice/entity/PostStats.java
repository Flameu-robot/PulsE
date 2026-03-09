package com.example.feedservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostStats {

    @Id
    private Long postId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "likes_count", nullable = false)
    @Builder.Default
    private int likesCount = 0;

    @Column(name = "comments_count", nullable = false)
    @Builder.Default
    private int commentsCount = 0;

    @Column(name = "shares_count", nullable = false)
    @Builder.Default
    private int sharesCount = 0;

    @Column(name = "views_count", nullable = false)
    @Builder.Default
    private int viewsCount = 0;

    public void incrementLikes()    { likesCount++;    }
    public void decrementLikes()    { likesCount = Math.max(0, likesCount - 1); }
    public void incrementComments() { commentsCount++; }
    public void decrementComments() { commentsCount = Math.max(0, commentsCount - 1); }
    public void incrementShares()   { sharesCount++;   }
    public void incrementViews()    { viewsCount++;    }
}
