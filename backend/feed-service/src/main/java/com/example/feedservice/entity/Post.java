package com.example.feedservice.entity;

import com.example.feedservice.entity.enums.PostVisibility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PostVisibility visibility = PostVisibility.PUBLIC;

    @Column(name = "is_pinned", nullable = false)
    @Builder.Default
    private boolean pinned = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private Set<PostAttachment> attachments = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostMusicLink> musicLinks = new LinkedHashSet<>();

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PostStats stats;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostGroup> postGroups = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addAttachment(PostAttachment attachment) {
        attachments.add(attachment);
        attachment.setPost(this);
    }

    public void removeAttachment(PostAttachment attachment) {
        attachments.remove(attachment);
        attachment.setPost(null);
    }

    public void addMusicLink(PostMusicLink link) {
        musicLinks.add(link);
        link.setPost(this);
    }

    public void addToGroup(Long groupId) {
        var pg = PostGroup.builder()
                .post(this)
                .groupId(groupId)
                .build();
        postGroups.add(pg);
    }

    public boolean isPublishedToGroups() {
        return !postGroups.isEmpty();
    }

    public List<Long> getGroupIds() {
        return postGroups.stream()
                .map(PostGroup::getGroupId)
                .toList();
    }
}