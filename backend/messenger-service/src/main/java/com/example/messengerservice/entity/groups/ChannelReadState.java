package com.example.messengerservice.entity.groups;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "channel_read_states",
        uniqueConstraints = @UniqueConstraint(
                name = "uc_channel_read_states_user_channel",
                columnNames = {"user_id", "channel_id"}
        ),
        indexes = {
                @Index(name = "idx_read_state_user_id", columnList = "user_id"),
                @Index(name = "idx_read_state_channel_id", columnList = "channel_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelReadState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private TextChannel channel;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}