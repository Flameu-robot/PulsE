package com.example.messengerservice.service;

import com.example.messengerservice.AbstractPostgresTC;
import com.example.messengerservice.TestDataFactory;
import com.example.messengerservice.dto.request.MessageRequest;
import com.example.messengerservice.dto.response.MessageResponse;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.entity.groups.TextChannel;
import com.example.messengerservice.repository.groups.GroupRepository;
import com.example.messengerservice.repository.messages.MessageRepository;
import com.example.shared.security.GatewayHeaderAuthFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MessageServiceIT extends AbstractPostgresTC {

    @MockitoBean
    GatewayHeaderAuthFilter gatewayHeaderAuthFilter;

    @Autowired MessageService messageService;
    @Autowired GroupRepository groupRepository;
    @Autowired TestDataFactory factory;
    @Autowired MessageRepository messageRepository;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void cleanup() {
        jdbc.execute("TRUNCATE TABLE messages, group_members, text_channels, chat_groups RESTART IDENTITY CASCADE");
    }

    @Test
    @DisplayName("Групповой чат: сообщение сохраняется в БД с правильным channel_id")
    void groupChat_messageSavedWithCorrectChannelId() {
        Group group = factory.savedGroup(1L);
        TextChannel channel = factory.savedChannel(group);
        factory.addMember(group, 1L);

        MessageResponse response = messageService.sendMessage(1L,
                new MessageRequest(group.getId(), channel.getId(), null, "hello"));

        messageRepository.findById(response.messageId()).ifPresentOrElse(
                saved -> assertThat(saved.getChannel().getId()).isEqualTo(channel.getId()),
                () -> { throw new AssertionError("Message not found in DB"); }
        );
    }

    @Test
    @DisplayName("Групповой чат: channel из другой группы отклоняется, сообщение не сохраняется")
    void groupChat_channelFromAnotherGroup_messageNotSaved() {
        Group group1 = factory.savedGroup(1L);
        Group group2 = factory.savedGroup(2L);
        TextChannel channelOfGroup2 = factory.savedChannel(group2);
        factory.addMember(group1, 1L);

        try {
            messageService.sendMessage(1L,
                    new MessageRequest(group1.getId(), channelOfGroup2.getId(), null, "hi"));
        } catch (Exception _) {
            // Без отправки сообщения
        }

        assertThat(messageRepository.count()).isZero();
    }

    @Test
    @DisplayName("DM: отправка 1-2 и 2-1 попадает в один и тот же чат")
    void dm_hashKeyIsSymmetric_sameGroupBothDirections() {
        MessageResponse from1 = messageService.sendMessage(1L,
                new MessageRequest(null, null, 2L, "from 1"));
        MessageResponse from2 = messageService.sendMessage(2L,
                new MessageRequest(null, null, 1L, "from 2"));

        assertThat(from1.groupId()).isEqualTo(from2.groupId());
        assertThat(groupRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("DM: повторная отправка в тот же чат - новая группа не создаётся")
    void dm_repeatedSend_noNewGroupCreated() {
        messageService.sendMessage(1L, new MessageRequest(null, null, 2L, "msg 1"));
        messageService.sendMessage(1L, new MessageRequest(null, null, 2L, "msg 2"));
        messageService.sendMessage(1L, new MessageRequest(null, null, 2L, "msg 3"));

        assertThat(groupRepository.count()).isEqualTo(1);
        assertThat(messageRepository.count()).isEqualTo(3);
    }
}