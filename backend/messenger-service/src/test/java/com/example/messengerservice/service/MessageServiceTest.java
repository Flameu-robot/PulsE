package com.example.messengerservice.service;

import com.example.messengerservice.dto.request.MessageRequest;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.entity.groups.TextChannel;
import com.example.messengerservice.entity.messages.Message;
import com.example.messengerservice.repository.groups.GroupMemberRepository;
import com.example.messengerservice.repository.groups.GroupRepository;
import com.example.messengerservice.repository.groups.TextChannelRepository;
import com.example.messengerservice.repository.messages.MessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock MessageRepository messageRepository;
    @Mock GroupMemberRepository memberRepository;
    @Mock TextChannelRepository channelRepository;
    @Mock GroupRepository groupRepository;
    @Mock GroupService groupService;

    @InjectMocks MessageService messageService;

    private static <T> T setField(T target, String name, Object value) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                f.set(target, value);
                return target;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Field '" + name + "' not found in class hierarchy of " + target.getClass().getName());
    }

    private static Group groupWithId(long id) {
        return setField(Group.builder().build(), "id", id);
    }

    private static TextChannel channelWithId(long id, Group group) {
        return setField(TextChannel.builder().group(group).name("general").build(), "id", id);
    }

    private static Message savedMessage(String content, TextChannel channel) {
        Message m = Message.builder().authorId(1L).content(content).channel(channel).build();
        setField(m, "id", 1L);
        setField(m, "createdAt", OffsetDateTime.now());
        return m;
    }

    @Test
    @DisplayName("Групповой чат: message сохраняется именно с тем channel, что вернул channelRepository")
    void groupChat_messageSavedWithCorrectChannel() {
        Group group = groupWithId(10L);
        TextChannel channel = channelWithId(20L, group);

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(memberRepository.existsByUserIdAndGroupId(1L, 10L)).thenReturn(true);
        when(channelRepository.findByIdAndGroupId(20L, 10L)).thenReturn(Optional.of(channel));
        when(messageRepository.save(any())).thenReturn(savedMessage("hi", channel));

        messageService.sendMessage(1L, new MessageRequest(10L, 20L, null, "hi"));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getChannel()).isSameAs(channel);
    }

    @Test
    @DisplayName("DM: Message сохраняется с каналом из findFirstByGroupId, а не из группового пути")
    void dm_messageSavedWithChannelFromDmGroup() {
        Group dmGroup = groupWithId(50L);
        TextChannel dmChannel = channelWithId(60L, dmGroup);

        when(groupService.getOrCreatePersonalChat(1L, 2L)).thenReturn(dmGroup);
        when(channelRepository.findFirstByGroupId(50L)).thenReturn(Optional.of(dmChannel));
        when(messageRepository.save(any())).thenReturn(savedMessage("hey", dmChannel));

        messageService.sendMessage(1L, new MessageRequest(null, null, 2L, "hey"));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getChannel()).isSameAs(dmChannel);
    }

    @Test
    @DisplayName("DM: getOrCreatePersonalChat вызывается с (senderId, targetUserId)")
    void dm_getOrCreateCalledWithCorrectArgumentOrder() {
        Long senderId = 5L;
        Long targetUserId = 3L;
        Group dmGroup = groupWithId(50L);
        TextChannel channel = channelWithId(60L, dmGroup);

        when(groupService.getOrCreatePersonalChat(senderId, targetUserId)).thenReturn(dmGroup);
        when(channelRepository.findFirstByGroupId(50L)).thenReturn(Optional.of(channel));
        when(messageRepository.save(any())).thenReturn(savedMessage("x", channel));

        messageService.sendMessage(senderId, new MessageRequest(null, null, targetUserId, "x"));

        verify(groupService).getOrCreatePersonalChat(senderId, targetUserId);
        verify(groupService, never()).getOrCreatePersonalChat(targetUserId, senderId);
    }

    @Test
    @DisplayName("DM: memberRepository не вызывается")
    void dm_memberRepositoryNeverCalled() {
        Group dmGroup = groupWithId(50L);
        TextChannel channel = channelWithId(60L, dmGroup);

        when(groupService.getOrCreatePersonalChat(1L, 2L)).thenReturn(dmGroup);
        when(channelRepository.findFirstByGroupId(50L)).thenReturn(Optional.of(channel));
        when(messageRepository.save(any())).thenReturn(savedMessage("dm", channel));

        messageService.sendMessage(1L, new MessageRequest(null, null, 2L, "dm"));

        verifyNoInteractions(memberRepository);
    }

    @Test
    @DisplayName("Групповой чат: groupService.getOrCreatePersonalChat не вызывается")
    void groupChat_groupServiceNeverCalled() {
        Group group = groupWithId(10L);
        TextChannel channel = channelWithId(20L, group);

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(memberRepository.existsByUserIdAndGroupId(1L, 10L)).thenReturn(true);
        when(channelRepository.findByIdAndGroupId(20L, 10L)).thenReturn(Optional.of(channel));
        when(messageRepository.save(any())).thenReturn(savedMessage("hi", channel));

        messageService.sendMessage(1L, new MessageRequest(10L, 20L, null, "hi"));

        verifyNoInteractions(groupService);
    }

    @Test
    @DisplayName("Групповой чат: messageRepository.save вызывается ровно один раз")
    void groupChat_saveCalledExactlyOnce() {
        Group group = groupWithId(10L);
        TextChannel channel = channelWithId(20L, group);

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(memberRepository.existsByUserIdAndGroupId(1L, 10L)).thenReturn(true);
        when(channelRepository.findByIdAndGroupId(20L, 10L)).thenReturn(Optional.of(channel));
        when(messageRepository.save(any())).thenReturn(savedMessage("hi", channel));

        messageService.sendMessage(1L, new MessageRequest(10L, 20L, null, "hi"));

        verify(messageRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("DM: messageRepository.save вызывается ровно один раз")
    void dm_saveCalledExactlyOnce() {
        Group dmGroup = groupWithId(50L);
        TextChannel channel = channelWithId(60L, dmGroup);

        when(groupService.getOrCreatePersonalChat(1L, 2L)).thenReturn(dmGroup);
        when(channelRepository.findFirstByGroupId(50L)).thenReturn(Optional.of(channel));
        when(messageRepository.save(any())).thenReturn(savedMessage("dm", channel));

        messageService.sendMessage(1L, new MessageRequest(null, null, 2L, "dm"));

        verify(messageRepository, times(1)).save(any());
    }
}
