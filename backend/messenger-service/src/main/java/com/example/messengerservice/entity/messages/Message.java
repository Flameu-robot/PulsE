package com.example.messengerservice.entity.messages;

import com.example.messengerservice.entity.base.BaseEntity;
import com.example.messengerservice.entity.groups.TextChannel;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_message_channel_id", columnList = "channel_id"),
        @Index(name = "idx_message_channel_created", columnList = "channel_id, created_at DESC"),
        @Index(name = "idx_message_author_id", columnList = "author_id"),
        @Index(name = "idx_message_reply_to", columnList = "reply_to_id"),
        @Index(name = "idx_message_created_at", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private TextChannel channel;

    @Column(columnDefinition = "text")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forward_from_id")
    private Message forwardFrom;

    @Column(name = "is_edited", nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean edited = false;

    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean deleted = false;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

    @AssertTrue(message = "Message must have content or attachments")
    @SuppressWarnings("unused")
    private boolean isValid() {
        return (content != null && !content.isBlank()) ||
                (attachments != null && !attachments.isEmpty());
    }
}