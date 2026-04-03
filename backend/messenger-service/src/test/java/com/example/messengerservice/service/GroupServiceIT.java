package com.example.messengerservice.service;

import com.example.messengerservice.entity.enums.GroupType;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.repository.groups.GroupMemberRepository;
import com.example.messengerservice.repository.groups.GroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
class GroupServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private GroupService groupService;
    @Autowired private GroupRepository groupRepository;
    @Autowired private GroupMemberRepository groupMemberRepository;

    @Test
    @DisplayName("Должен выкинуть exception при нарушении dmHashKey")
    void personalChat_UniqueConstraint() {
        groupService.getOrCreatePersonalChat(1L, 2L);

        Group duplicate = Group.builder()
                .dmHashKey("1:2")
                .type(GroupType.PERSONAL)
                .build();

        assertThrows(DataIntegrityViolationException.class,
                () -> groupRepository.saveAndFlush(duplicate));
    }

    @Test
    @DisplayName("Повторный вызов возвращает тот же чат")
    void personalChat_ShouldReturnSameChat() {
        Group first = groupService.getOrCreatePersonalChat(1L, 2L);
        Group second = groupService.getOrCreatePersonalChat(1L, 2L);

        assertEquals(first.getId(), second.getId());
        assertEquals(1, groupRepository.findAll().stream()
                .filter(g -> g.getType() == GroupType.PERSONAL)
                .count());
    }

    @Test
    @DisplayName("Симметричность: (1,2) и (2,1) — один и тот же чат")
    void personalChat_ShouldBeSymmetric() {
        Group ab = groupService.getOrCreatePersonalChat(1L, 2L);
        Group ba = groupService.getOrCreatePersonalChat(2L, 1L);

        assertEquals(ab.getId(), ba.getId());
    }

    @Test
    @DisplayName("Личный чат создаётся с каналом и двумя участниками")
    void personalChat_ShouldHaveChannelAndMembers() {
        Group chat = groupService.getOrCreatePersonalChat(1L, 2L);

        assertFalse(chat.getChannels().isEmpty());
        assertEquals(2, groupMemberRepository.findAllByGroup(chat).size());
    }
}