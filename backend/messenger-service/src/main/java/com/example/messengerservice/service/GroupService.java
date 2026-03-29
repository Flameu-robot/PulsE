package com.example.messengerservice.service;

import com.example.messengerservice.entity.enums.GroupFeatures;
import com.example.messengerservice.entity.enums.GroupType;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.entity.groups.GroupMember;
import com.example.messengerservice.repository.groups.GroupMemberRepository;
import com.example.messengerservice.repository.groups.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public Group getOrCreatePersonalChat(Long user1, Long user2) {
        // Ключ "minId:maxId"
        String hashKey = (user1 < user2) ? user1 + ":" + user2 : user2 + ":" + user1;

        return groupRepository.findByDmHashKey(hashKey)
                .orElseGet(() -> {
                    log.info("Creating new personal chat for hash: {}", hashKey);
                    Group chat = Group.builder()
                            .type(GroupType.PERSONAL)
                            .dmHashKey(hashKey)
                            .features(new GroupFeatures())
                            .build();

                    Group saved = groupRepository.save(chat);

                    addMemberToGroup(saved, user1);
                    addMemberToGroup(saved, user2);

                    return saved;
                });
    }

    @Transactional
    public Group createGroup(Long ownerId, String name, List<Long> initialMembers) {
        Group group = Group.builder()
                .name(name)
                .ownerId(ownerId)
                .type(GroupType.GROUP)
                .features(new GroupFeatures()) // Группа без фич
                .build();

        Group saved = groupRepository.save(group);

        addMemberToGroup(saved, ownerId);

        if (initialMembers != null) {
            initialMembers.forEach(memberId -> addMemberToGroup(saved, memberId));
        }

        return saved;
    }

    @Transactional
    public Group createServer(Long ownerId, String name) {
        GroupFeatures features = new GroupFeatures();
        features.setRolesEnabled(true); // Кастом роли

        Group server = Group.builder()
                .name(name)
                .ownerId(ownerId)
                .type(GroupType.SERVER)
                .features(features)
                .build();

        Group saved = groupRepository.save(server);
        addMemberToGroup(saved, ownerId);

        return saved;
    }

    private void addMemberToGroup(Group group, Long userId) {
        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUserId(userId);
        member.setPermissions(0L); // Заглушка
        groupMemberRepository.save(member);
    }
}
