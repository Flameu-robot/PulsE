package com.example.messengerservice.entity.groups;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "channel_read_states",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "channel_id"}))
@Getter
@Setter
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

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}