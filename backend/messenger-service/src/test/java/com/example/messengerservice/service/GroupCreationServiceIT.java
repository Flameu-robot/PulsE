package com.example.messengerservice.service;

import com.example.messengerservice.AbstractPostgresTC;
import com.example.messengerservice.dto.request.GroupRequest;
import com.example.messengerservice.entity.enums.GroupType;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.repository.groups.GroupMemberRepository;
import com.example.messengerservice.repository.groups.GroupRepository;
import com.example.messengerservice.repository.groups.TextChannelRepository;
import com.example.shared.security.GatewayHeaderAuthFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class GroupCreationServiceIT extends AbstractPostgresTC {

    @MockitoBean
    GatewayHeaderAuthFilter gatewayHeaderAuthFilter;

    @Autowired GroupCreationService groupCreationService;
    @Autowired GroupRepository groupRepository;
    @Autowired GroupMemberRepository groupMemberRepository;
    @Autowired
    TextChannelRepository channelRepository;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void cleanup() {
        jdbc.execute("TRUNCATE TABLE group_members, text_channels, chat_groups RESTART IDENTITY CASCADE");
    }

    @Test
    @DisplayName("createPersonalChat: реально коммитит группу, канал и 2 участников")
    void createPersonalChat_shouldPersistGroupChannelAndMembers() {
        Group chat = groupCreationService.createPersonalChat("1:2", 1L, 2L);

        assertTrue(groupRepository.findByDmHashKey("1:2").isPresent());

        assertEquals(1, channelRepository.countByGroupId(chat.getId()));

        assertEquals(2, groupMemberRepository.countByGroupId(chat.getId()));
    }

    @Test
    @DisplayName("createGroup: создаёт группу + канал + owner + initialMembers в БД")
    void createGroup_shouldPersistAll() {
        GroupRequest req = new GroupRequest(GroupType.GROUP, "my-group", List.of(2L, 3L));

        Group group = groupCreationService.createGroup(1L, req);

        assertTrue(groupRepository.existsById(group.getId()));

        assertEquals(1, channelRepository.countByGroupId(group.getId()));

        assertEquals(3, groupMemberRepository.countByGroupId(group.getId()));
    }

    @Test
    @DisplayName("createGroup без initialMembers: только owner добавлен")
    void createGroup_withoutInitialMembers_onlyOwner() {
        GroupRequest req = new GroupRequest(GroupType.GROUP, "solo-group", null);

        Group group = groupCreationService.createGroup(5L, req);

        assertEquals(1, groupMemberRepository.countByGroupId(group.getId()));
    }

    @Test
    @DisplayName("createServer: сохраняет server с rolesEnabled=true, каналом и owner")
    void createServer_shouldPersistWithRolesEnabled() {
        Group server = groupCreationService.createServer(1L, "Dev Server");

        assertEquals(GroupType.SERVER, server.getType());
        assertEquals("Dev Server", server.getName());

        Group fromDb = groupRepository.findById(server.getId()).orElseThrow();
        assertTrue(fromDb.getFeatures().isRolesEnabled());

        assertEquals(1, channelRepository.countByGroupId(server.getId()));

        assertEquals(1, groupMemberRepository.countByGroupId(server.getId()));
    }
}