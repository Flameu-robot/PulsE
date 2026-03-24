package com.example.messengerservice.entity.groups;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "text_channels")
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

    @Column(name = "is_deleted")
    private boolean deleted = false;
}