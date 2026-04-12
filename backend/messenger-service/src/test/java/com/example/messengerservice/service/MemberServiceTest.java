package com.example.messengerservice.service;

import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.repository.groups.GroupMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock GroupMemberRepository memberRepository;

    @InjectMocks MemberService memberService;

    private static Group groupWithId() {
        Group g = Group.builder().build();
        try {
            Field f = g.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(g, 10L);
            return g;
        } catch (Exception e) {
            throw new RuntimeException("Cannot set id via reflection; add builder id or setter", e);
        }
    }

    @Test
    @DisplayName("Добавляет участника в группу")
    void addMemberToGroup_whenInserted_callsInsertIgnore() {
        Group group = groupWithId();
        when(memberRepository.insertIgnore(10L, 7L, 0L)).thenReturn(1);

        memberService.addMemberToGroup(group, 7L);

        verify(memberRepository).insertIgnore(10L, 7L, 0L);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("Добавление уже существующего участника не выкидывает Exception")
    void addMemberToGroup_whenAlreadyExists_returns0_noException() {
        Group group = groupWithId();
        when(memberRepository.insertIgnore(10L, 7L, 0L)).thenReturn(0);

        memberService.addMemberToGroup(group, 7L);

        verify(memberRepository).insertIgnore(10L, 7L, 0L);
        verifyNoMoreInteractions(memberRepository);
    }
}