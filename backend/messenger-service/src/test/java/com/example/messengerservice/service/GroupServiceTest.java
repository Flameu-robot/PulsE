package com.example.messengerservice.service;

import com.example.messengerservice.dto.request.GroupRequest;
import com.example.messengerservice.entity.enums.GroupType;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.repository.groups.GroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private MemberService memberService;
    @Mock
    private ChannelService channelService;

    @InjectMocks
    private GroupService groupService;

    @Test
    @DisplayName("Возвращает существующий чат без создания нового")
    void getOrCreate_ShouldReturnExisting() {
        String expectedHash = "5:10";
        Group existing = Group.builder().id(1L).dmHashKey(expectedHash).build();
        when(groupRepository.findByDmHashKey(expectedHash)).thenReturn(Optional.of(existing));

        Group result = groupService.getOrCreatePersonalChat(10L, 5L);

        assertEquals(existing, result);
        verify(groupRepository, never()).save(any());
        verify(groupRepository, never()).saveAndFlush(any());
        verify(memberService, never()).addMemberToGroup(any(), any());
    }

    @Test
    @DisplayName("Должен нормализовать хеш чата")
    void getOrCreate_ShouldBeSymmetric() {
        String expectedHash = "100:200";
        Group existing = Group.builder().id(1L).dmHashKey(expectedHash).build();
        when(groupRepository.findByDmHashKey(expectedHash)).thenReturn(Optional.of(existing));

        groupService.getOrCreatePersonalChat(100L, 200L);
        groupService.getOrCreatePersonalChat(200L, 100L);

        verify(groupRepository, times(2)).findByDmHashKey(expectedHash);
        verify(groupRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Должен создать чат с каналом и участниками")
    void getOrCreate_ShouldCreateNewChat() {
        String expectedHash = "1:2";
        Group savedGroup = Group.builder()
                .id(1L)
                .dmHashKey(expectedHash)
                .type(GroupType.PERSONAL)
                .build();

        when(groupRepository.findByDmHashKey(expectedHash)).thenReturn(Optional.empty());
        when(groupRepository.saveAndFlush(any())).thenReturn(savedGroup);

        Group result = groupService.getOrCreatePersonalChat(1L, 2L);

        assertNotNull(result);
        assertEquals(GroupType.PERSONAL, result.getType());
        verify(channelService, times(1)).createChannel(savedGroup, "general");
        verify(memberService, times(1)).addMemberToGroup(savedGroup, 1L);
        verify(memberService, times(1)).addMemberToGroup(savedGroup, 2L);
    }

    @Test
    @DisplayName("Должен создать группу с каналом и овнером в участниках")
    void createChat_Group_Success() {
        Long ownerId = 1L;
        GroupRequest request = new GroupRequest(GroupType.GROUP, "Test Group", null);

        when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        groupService.createChat(ownerId, request);

        verify(groupRepository, times(1)).save(argThat(group ->
                group.getType() == GroupType.GROUP &&
                        group.getName().equals("Test Group") &&
                        group.getOwnerId().equals(ownerId)
        ));
        verify(channelService, times(1)).createChannel(any(), eq("general"));
        verify(memberService, times(1)).addMemberToGroup(any(), eq(ownerId));  // ← обновил
    }

    @Test
    @DisplayName("Должен добавить начальных участников")
    void createChat_Group_ShouldNotDuplicateOwner() {
        Long ownerId = 1L;
        GroupRequest request = new GroupRequest(GroupType.GROUP, "Test Group", List.of(1L, 2L, 3L));

        when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        groupService.createChat(ownerId, request);

        verify(memberService, times(3)).addMemberToGroup(any(), any());
    }

    @Test
    @DisplayName("Должен убрать дупликаты участников")
    void createChat_Group_ShouldDeduplicateMembers() {
        Long ownerId = 1L;
        GroupRequest request = new GroupRequest(GroupType.GROUP, "Test", List.of(2L, 2L, 3L));

        when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        groupService.createChat(ownerId, request);

        verify(memberService, times(3)).addMemberToGroup(any(), any());
    }

    @Test
    @DisplayName("Должен создать сервер с включенными ролями")
    void createChat_Server_Success() {
        Long ownerId = 1L;
        GroupRequest request = new GroupRequest(GroupType.SERVER, "Dev Server", null);

        when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        groupService.createChat(ownerId, request);

        verify(groupRepository, times(1)).save(argThat(group ->
                group.getType() == GroupType.SERVER &&
                        group.getFeatures().isRolesEnabled() &&
                        group.getName().equals("Dev Server")
        ));
        verify(channelService, times(1)).createChannel(any(), eq("general"));
        verify(memberService, times(1)).addMemberToGroup(any(), eq(ownerId));
    }

    @Test
    @DisplayName("Должен выкинуть exception с неправильным типом группы")
    void createChat_ShouldThrowOnPersonalType() {
        GroupRequest request = new GroupRequest(GroupType.PERSONAL, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> groupService.createChat(1L, request));
    }
}