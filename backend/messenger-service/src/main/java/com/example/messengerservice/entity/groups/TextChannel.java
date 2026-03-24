package com.example.messengerservice.entity.groups;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "text_channels", indexes = {
        @Index(name = "idx_channel_group_id", columnList = "group_id"),
        @Index(name = "idx_channel_group_position", columnList = "group_id, position")
})
@Getter
@Setter
public class TextChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String topic;

    private Integer position;

    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("false")
    private boolean deleted = false;
}