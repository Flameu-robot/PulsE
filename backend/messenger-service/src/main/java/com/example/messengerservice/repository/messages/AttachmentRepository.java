package com.example.messengerservice.repository.messages;

import com.example.messengerservice.entity.messages.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
