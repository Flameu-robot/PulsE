package com.example.messengerservice.service;

import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.entity.groups.GroupMember;
import com.example.messengerservice.repository.groups.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final GroupMemberRepository memberRepository;

    @Transactional
    public void addMemberToGroup(Group group, Long userId) {
        boolean alreadyMember = memberRepository.existsByGroupAndUserId(group, userId);
        if (alreadyMember) {
            log.warn("User {} is already a member of group {}", userId, group.getId());
            return;
        }
        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUserId(userId);
        member.setPermissions(0L); // Заглушка
        memberRepository.save(member);
    }
}
