package com.example.messengerservice.repository;

import com.example.messengerservice.AbstractPostgresTC;
import com.example.messengerservice.entity.enums.GroupType;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.repository.groups.GroupMemberRepository;
import com.example.messengerservice.repository.groups.GroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberRepositoryIT extends AbstractPostgresTC {

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    GroupMemberRepository groupMemberRepository;

    private Group newGroup() {
        Group g = Group.builder()
                .name("g")
                .type(GroupType.GROUP)
                .build();
        return groupRepository.saveAndFlush(g);
    }

    @Test
    @DisplayName("Разные пары занесутся в БД без ошибок")
    void insertAllAndReturns1() {
        Group g = newGroup();

        int first = groupMemberRepository.insertIgnore(g.getId(), 10L, 0L);
        int second = groupMemberRepository.insertIgnore(g.getId(), 11L, 0L);

        assertThat(first).isEqualTo(1);
        assertThat(second).isEqualTo(1);
        assertThat(groupMemberRepository.countByGroupId(g.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Повтор пары возвращает 0")
    void insertIgnore_duplicate_returns0() {
        Group g = newGroup();

        int first = groupMemberRepository.insertIgnore(g.getId(), 10L, 0L);
        int second = groupMemberRepository.insertIgnore(g.getId(), 10L, 0L);

        assertThat(first).isEqualTo(1);
        assertThat(second).isZero();
        assertThat(groupMemberRepository.countByGroupId(g.getId())).isEqualTo(1);
    }
}