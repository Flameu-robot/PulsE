package com.example.messengerservice.service;

import com.example.messengerservice.AbstractPostgresTC;
import com.example.messengerservice.entity.enums.GroupFeatures;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MemberServiceIT extends AbstractPostgresTC {

    @MockitoBean
    GatewayHeaderAuthFilter gatewayHeaderAuthFilter;

    @Autowired MemberService memberService;
    @Autowired GroupRepository groupRepository;
    @Autowired GroupMemberRepository groupMemberRepository;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void cleanup() {
        jdbc.execute("TRUNCATE TABLE group_members, text_channels, chat_groups RESTART IDENTITY CASCADE");
    }

    private Group savedGroup() {
        return groupRepository.saveAndFlush(
                Group.builder()
                        .type(GroupType.GROUP)
                        .name("test-group")
                        .ownerId(1L)
                        .features(new GroupFeatures())
                        .build()
        );
    }

    @Test
    @DisplayName("insertIgnore: новый участник реально сохраняется в PostgreSQL")
    void addMember_shouldPersist() {
        Group group = savedGroup();

        memberService.addMemberToGroup(group, 10L);

        assertEquals(1, groupMemberRepository.countByGroupId(group.getId()));
    }

    @Test
    @DisplayName("insertIgnore: дублирующий вызов не создаёт вторую запись")
    void addMember_duplicate_shouldNotCreateSecondRow() {
        Group group = savedGroup();

        memberService.addMemberToGroup(group, 10L);
        memberService.addMemberToGroup(group, 10L);

        assertEquals(1, groupMemberRepository.countByGroupId(group.getId()));
    }

    @Test
    @DisplayName("insertIgnore: два разных userId добавляются независимо")
    void addMember_twoUsers_shouldBothPersist() {
        Group group = savedGroup();

        memberService.addMemberToGroup(group, 10L);
        memberService.addMemberToGroup(group, 20L);

        assertEquals(2, groupMemberRepository.countByGroupId(group.getId()));
    }
}