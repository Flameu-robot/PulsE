package com.example.feedservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_music_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMusicLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @Column(name = "listen_count", nullable = false)
    @Builder.Default
    private int listenCount = 0;
}