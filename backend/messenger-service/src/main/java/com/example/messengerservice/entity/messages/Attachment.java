package com.example.messengerservice.entity.messages;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "attachments", indexes = {
        @Index(name = "idx_attachment_message_id", columnList = "message_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type",nullable = false)
    private String fileType;

    @Column(name = "file_size",nullable = false)
    private Long fileSize;

    @Column(name = "minio_object_name",nullable = false)
    private String minioObjectName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    @ColumnDefault("now()")
    private OffsetDateTime createdAt;
}