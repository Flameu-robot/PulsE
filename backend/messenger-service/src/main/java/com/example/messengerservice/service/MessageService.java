package com.example.messengerservice.service;

import com.example.messengerservice.dto.request.MessageRequest;
import com.example.messengerservice.dto.response.MessageResponse;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.entity.messages.Message;
import com.example.messengerservice.repository.groups.GroupRepository;
import com.example.messengerservice.repository.messages.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;
    private final GroupService groupService;

    @Transactional
    public MessageResponse sendMessage(Long senderId, MessageRequest request) {
        Group targetGroup;

        if (request.groupId() != null) {
            targetGroup = groupRepository.findById(request.groupId())
                    .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

            // TODO: Здесь должна быть проверка, что senderId является участником этой группы

        } else {
            targetGroup = groupService.getOrCreatePersonalChat(senderId, request.targetUserId());
        }

        Message message = Message.builder()
                .authorId(senderId)
                .content(request.text())
                .channel(targetGroup.getChannels().getFirst())
                .build();

        Message saved = messageRepository.save(message);
        return new MessageResponse(
                saved.getId(),
                targetGroup.getId(),
                saved.getChannel().getId(),
                senderId,
                saved.getContent(),
                saved.getCreatedAt()
        );
    }
}
