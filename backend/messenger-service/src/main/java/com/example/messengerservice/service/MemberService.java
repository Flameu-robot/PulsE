package com.example.messengerservice.service;

import com.example.messengerservice.entity.groups.Group;
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
        int inserted = memberRepository.insertIgnore(group.getId(), userId, 0L);
        if (inserted == 0) {
            log.debug("User {} already member of group {}", userId, group.getId());
        }
    }
}
