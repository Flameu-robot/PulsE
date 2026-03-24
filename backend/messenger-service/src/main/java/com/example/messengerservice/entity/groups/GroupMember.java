package com.example.messengerservice.entity.groups;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "group_members",
        uniqueConstraints = @UniqueConstraint(
                name = "uc_group_members_group_user",
                columnNames = {"group_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_group_member_user_id", columnList = "user_id"),
                @Index(name = "idx_group_member_group_id", columnList = "group_id"),
                @Index(name = "idx_group_member_joined_at", columnList = "joined_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private long permissions = 0L;

    @CreationTimestamp
    @Column(name = "joined_at")
    private OffsetDateTime joinedAt;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean muted = false;
}