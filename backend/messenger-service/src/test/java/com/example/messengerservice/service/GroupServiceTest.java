package com.example.messengerservice.service;

import com.example.messengerservice.entity.enums.GroupType;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.repository.groups.GroupMemberRepository;
import com.example.messengerservice.repository.groups.GroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private GroupService groupService;

    @Test
    @DisplayName("Should correctly sort IDs and find existing personal chat")
    void getOrCreatePersonalChat_Existing() {
        Long userA = 10L;
        Long userB = 5L;
        String expectedHash = "5:10"; // 5 < 10
        Group existingGroup = Group.builder().id(1L).dmHashKey(expectedHash).build();

        when(groupRepository.findByDmHashKey(expectedHash)).thenReturn(Optional.of(existingGroup));

        Group result = groupService.getOrCreatePersonalChat(userA, userB);

        assertEquals(existingGroup, result);
        verify(groupRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create new server with roles enabled")
    void createServer_Success() {
        Long ownerId = 1L;
        String name = "Dev Server";
        when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Group result = groupService.createServer(ownerId, name);

        assertNotNull(result);
        assertTrue(result.getFeatures().isRolesEnabled());
        assertEquals(GroupType.SERVER, result.getType());
        verify(groupMemberRepository, times(1)).save(any());
    }

    @Test
    void getOrCreatePersonalChat_ShouldBeSymmetric() {
        Long userA = 100L;
        Long userB = 200L;
        String expectedHash = "100:200";

        groupService.getOrCreatePersonalChat(userA, userB);
        groupService.getOrCreatePersonalChat(userB, userA);

        verify(groupRepository, times(2)).findByDmHashKey(expectedHash);
    }
}