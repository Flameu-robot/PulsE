package com.example.messengerservice.service;

import com.example.messengerservice.dto.request.GroupRequest;
import com.example.messengerservice.entity.enums.GroupType;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.repository.groups.GroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupCreationServiceTest {

    @Mock GroupRepository groupRepository;
    @Mock ChannelService channelService;
    @Mock MemberService memberService;

    @InjectMocks GroupCreationService groupCreationService;

    private static Group withId(Group g, long id) {
        try {
            Field f = g.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(g, id);
            return g;
        } catch (Exception e) {
            throw new RuntimeException("Cannot set id via reflection; add builder id or setter", e);
        }
    }

    @Test
    @DisplayName("Создает личный чат с каналом и 2 участниками")
    void createPersonalChat_success_createsChannelAndTwoMembers() {
        String hashKey = "1:2";

        Group saved = withId(Group.builder()
                .dmHashKey(hashKey)
                .type(GroupType.PERSONAL)
                .build(), 100L);

        when(groupRepository.saveAndFlush(any(Group.class))).thenReturn(saved);

        Group res = groupCreationService.createPersonalChat(hashKey, 1L, 2L);

        assertSame(saved, res);
        verify(groupRepository).saveAndFlush(argThat(g ->
                g.getType() == GroupType.PERSONAL && hashKey.equals(g.getDmHashKey())
        ));
        verify(channelService).createChannel(saved, "general");
        verify(memberService).addMemberToGroup(saved, 1L);
        verify(memberService).addMemberToGroup(saved, 2L);
    }

    @Test
    @DisplayName("Создание личного чата с дубликатом - без ошибок")
    void createPersonalChat_whenUniqueViolation_returnsExisting_doesNotCreateSideEffects() {
        String hashKey = "1:2";
        when(groupRepository.saveAndFlush(any(Group.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        Group existing = Group.builder()
                .dmHashKey(hashKey)
                .type(GroupType.PERSONAL)
                .build();

        when(groupRepository.findByDmHashKey(hashKey)).thenReturn(Optional.of(existing));

        Group res = groupCreationService.createPersonalChat(hashKey, 1L, 2L);

        assertSame(existing, res);

        verify(groupRepository).findByDmHashKey(hashKey);
        verify(channelService, never()).createChannel(any(), anyString());
        verify(memberService, never()).addMemberToGroup(any(), anyLong());
    }

    @Test
    @DisplayName("Выбрасывает Not found при дубликате, но при этом чат не был найден")
    void createPersonalChat_whenUniqueViolation_butNotFound_throws() {
        String hashKey = "1:2";
        when(groupRepository.saveAndFlush(any(Group.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));
        when(groupRepository.findByDmHashKey(hashKey)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> groupCreationService.createPersonalChat(hashKey, 1L, 2L));
    }

    @Test
    @DisplayName("Создает группу, добавляет владельца и участников (с обрезкой повторов)")
    void createGroup_addsOwner_andDistinctInitialMembersWithoutOwner() {
        var req = new GroupRequest(GroupType.GROUP, "g1", List.of(1L, 2L, 2L, 3L));
        Group saved = withId(Group.builder().type(GroupType.GROUP).name("g1").ownerId(1L).build(), 200L);

        when(groupRepository.save(any(Group.class))).thenReturn(saved);

        Group res = groupCreationService.createGroup(1L, req);

        assertSame(saved, res);
        verify(channelService).createChannel(saved, "general");

        verify(memberService).addMemberToGroup(saved, 1L);
        verify(memberService).addMemberToGroup(saved, 2L);
        verify(memberService).addMemberToGroup(saved, 3L);

        verify(memberService, times(3)).addMemberToGroup(eq(saved), anyLong());
    }

    @Test
    @DisplayName("Создает сервер с каналом и владельцем")
    void createServer_createsServerWithChannelAndOwnerMember() {
        Group saved = withId(Group.builder()
                .type(GroupType.SERVER)
                .name("s1")
                .ownerId(5L)
                .build(), 300L);

        when(groupRepository.save(any(Group.class))).thenReturn(saved);

        Group res = groupCreationService.createServer(5L, "s1");

        assertSame(saved, res);
        verify(channelService).createChannel(saved, "general");
        verify(memberService).addMemberToGroup(saved, 5L);

        verify(groupRepository).save(argThat(g ->
                g.getType() == GroupType.SERVER &&
                        "s1".equals(g.getName()) &&
                        Long.valueOf(5L).equals(g.getOwnerId()) &&
                        g.getFeatures() != null
        ));
    }
}