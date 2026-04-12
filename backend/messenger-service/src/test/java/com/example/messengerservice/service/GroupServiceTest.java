package com.example.messengerservice.service;

import com.example.messengerservice.dto.request.GroupRequest;
import com.example.messengerservice.dto.response.GroupResponse;
import com.example.messengerservice.entity.enums.GroupType;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.repository.groups.GroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock private GroupRepository groupRepository;
    @Mock private GroupCreationService groupCreationService;

    @InjectMocks private GroupService groupService;

    @Test
    @DisplayName("Возвращает существующий личный чат и не создаёт новый")
    void getOrCreate_shouldReturnExisting() {
        String hash = "5:10";
        Group existing = Group.builder()
                .dmHashKey(hash)
                .type(GroupType.PERSONAL)
                .build();

        when(groupRepository.findByDmHashKey(hash)).thenReturn(Optional.of(existing));

        Group result = groupService.getOrCreatePersonalChat(10L, 5L);

        assertSame(existing, result);
        verify(groupRepository).findByDmHashKey(hash);
        verifyNoInteractions(groupCreationService);
    }

    @Test
    @DisplayName("Нормализует hashKey: (a,b) и (b,a) ищут один ключ")
    void getOrCreate_shouldNormalizeHashKey() {
        String hash = "100:200";
        when(groupRepository.findByDmHashKey(hash)).thenReturn(Optional.of(Group.builder().build()));

        groupService.getOrCreatePersonalChat(100L, 200L);
        groupService.getOrCreatePersonalChat(200L, 100L);

        verify(groupRepository, times(2)).findByDmHashKey(hash);
        verifyNoInteractions(groupCreationService);
    }

    @Test
    @DisplayName("Если чата нет - делегирует создание в GroupCreationService")
    void getOrCreate_whenMissing_delegatesToCreationService() {
        String hash = "1:2";
        when(groupRepository.findByDmHashKey(hash)).thenReturn(Optional.empty());

        Group created = Group.builder()
                .dmHashKey(hash)
                .type(GroupType.PERSONAL)
                .build();

        when(groupCreationService.createPersonalChat(hash, 1L, 2L)).thenReturn(created);

        Group result = groupService.getOrCreatePersonalChat(1L, 2L);

        assertSame(created, result);
        verify(groupRepository).findByDmHashKey(hash);
        verify(groupCreationService).createPersonalChat(hash, 1L, 2L);
    }

    @Test
    @DisplayName("createChat(GROUP) - делегирует в createGroup")
    void createChat_group_delegates() {
        Long ownerId = 1L;
        GroupRequest req = new GroupRequest(GroupType.GROUP, "Test Group", null);

        Group saved = Group.builder().type(GroupType.GROUP).name("Test Group").ownerId(ownerId).build();
        when(groupCreationService.createGroup(ownerId, req)).thenReturn(saved);

        GroupResponse resp = groupService.createChat(ownerId, req);

        assertEquals(GroupType.GROUP, resp.type());
        verify(groupCreationService).createGroup(ownerId, req);
        verify(groupCreationService, never()).createServer(anyLong(), anyString());
    }

    @Test
    @DisplayName("createChat(SERVER) - делегирует в createServer")
    void createChat_server_delegates() {
        Long ownerId = 1L;
        GroupRequest req = new GroupRequest(GroupType.SERVER, "Dev Server", null);

        Group saved = Group.builder().type(GroupType.SERVER).name("Dev Server").ownerId(ownerId).build();
        when(groupCreationService.createServer(ownerId, "Dev Server")).thenReturn(saved);

        GroupResponse resp = groupService.createChat(ownerId, req);

        assertEquals(GroupType.SERVER, resp.type());
        verify(groupCreationService).createServer(ownerId, "Dev Server");
        verify(groupCreationService, never()).createGroup(anyLong(), any());
    }

    @Test
    @DisplayName("createChat(PERSONAL) -> IllegalArgumentException")
    void createChat_personal_throws() {
        GroupRequest req = new GroupRequest(GroupType.PERSONAL, null, null);
        assertThrows(IllegalArgumentException.class, () -> groupService.createChat(1L, req));
        verifyNoInteractions(groupCreationService);
    }
}