package com.example.messengerservice.service;

import com.example.messengerservice.dto.request.MessageRequest;
import com.example.messengerservice.dto.response.MessageResponse;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.entity.groups.TextChannel;
import com.example.messengerservice.entity.messages.Message;
import com.example.messengerservice.exception.ChannelNotFoundException;
import com.example.messengerservice.exception.UserNotMemberException;
import com.example.messengerservice.repository.groups.GroupMemberRepository;
import com.example.messengerservice.repository.groups.GroupRepository;
import com.example.messengerservice.repository.groups.TextChannelRepository;
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
    private final GroupMemberRepository memberRepository;
    private final TextChannelRepository channelRepository;
    private final GroupRepository groupRepository;
    private final GroupService groupService;

    @Transactional
    public MessageResponse sendMessage(Long senderId, MessageRequest request) {
        Group targetGroup;
        TextChannel channel;

        // Чат является группой/сервером - выбор канала
        if (request.groupId() != null) {
            targetGroup = groupRepository.findById(request.groupId())
                    .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

            if (!memberRepository.existsByUserIdAndGroupId(senderId, targetGroup.getId())) {
                throw new UserNotMemberException(senderId, targetGroup.getId());
            }

            channel = channelRepository.findByIdAndGroupId(request.channelId(), targetGroup.getId())
                    .orElseThrow(() -> new ChannelNotFoundException(request.channelId(), request.groupId()));
        } else { // Личные сообщения - канал только один
            targetGroup = groupService.getOrCreatePersonalChat(senderId, request.targetUserId());

            channel = channelRepository.findFirstByGroupId(targetGroup.getId())
                    .orElseThrow(() -> new ChannelNotFoundException(targetGroup.getId()));
        }

        Message message = Message.builder()
                .authorId(senderId)
                .content(request.text())
                .channel(channel)
                .build();

        Message saved = messageRepository.save(message);
        return new MessageResponse(
                saved.getId(),
                targetGroup.getId(),
                channel.getId(),
                senderId,
                saved.getContent(),
                saved.getCreatedAt()
        );
    }
}
