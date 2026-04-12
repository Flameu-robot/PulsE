package com.example.messengerservice.service;

import com.example.messengerservice.AbstractPostgresTC;
import com.example.messengerservice.entity.enums.GroupType;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.repository.groups.GroupMemberRepository;
import com.example.messengerservice.repository.groups.GroupRepository;
import com.example.shared.security.GatewayHeaderAuthFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class GroupServiceIT extends AbstractPostgresTC {

    @MockitoBean
    GatewayHeaderAuthFilter gatewayHeaderAuthFilter;

    @Autowired GroupService groupService;
    @Autowired GroupRepository groupRepository;
    @Autowired GroupMemberRepository groupMemberRepository;

    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void cleanup() {
        jdbc.execute("TRUNCATE TABLE group_members, text_channels, chat_groups RESTART IDENTITY CASCADE");
    }

    @Test
    @DisplayName("Повторный вызов возвращает тот же personal chat (idempotent)")
    void personalChat_shouldReturnSameChat() {
        Group first = groupService.getOrCreatePersonalChat(1L, 2L);
        Group second = groupService.getOrCreatePersonalChat(1L, 2L);

        assertEquals(first.getId(), second.getId());

        long personalCount = groupRepository.findAll().stream()
                .filter(g -> g.getType() == GroupType.PERSONAL)
                .count();
        assertEquals(1, personalCount);
    }

    @Test
    @DisplayName("Симметричность: (1,2) и (2,1) — один и тот же чат")
    void personalChat_shouldBeSymmetric() {
        Group ab = groupService.getOrCreatePersonalChat(1L, 2L);
        Group ba = groupService.getOrCreatePersonalChat(2L, 1L);

        assertEquals(ab.getId(), ba.getId());
    }

    @Test
    @DisplayName("Уникальность dmHashKey обеспечивается БД")
    void personalChat_uniqueConstraint_dmHashKey() {
        groupService.getOrCreatePersonalChat(1L, 2L);

        Group duplicate = Group.builder()
                .dmHashKey("1:2")
                .type(GroupType.PERSONAL)
                .build();

        assertThrows(DataIntegrityViolationException.class,
                () -> groupRepository.saveAndFlush(duplicate));
    }

    @Test
    @DisplayName("Создаётся 2 участника (а дубликаты не вставляются)")
    void personalChat_shouldHaveTwoMembers() {
        Group chat = groupService.getOrCreatePersonalChat(1L, 2L);

        assertEquals(2, groupMemberRepository.countByGroupId(chat.getId()));
    }
}