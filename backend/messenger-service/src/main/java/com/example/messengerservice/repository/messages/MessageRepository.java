package com.example.messengerservice.repository.messages;

import com.example.messengerservice.entity.messages.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
