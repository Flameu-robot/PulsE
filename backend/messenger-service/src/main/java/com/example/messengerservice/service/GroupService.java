package com.example.messengerservice.service;

import com.example.messengerservice.dto.request.GroupRequest;
import com.example.messengerservice.entity.enums.GroupFeatures;
import com.example.messengerservice.entity.enums.GroupType;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.entity.groups.GroupMember;
import com.example.messengerservice.repository.groups.GroupMemberRepository;
import com.example.messengerservice.repository.groups.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ChannelService channelService;

    @Transactional
    public Group getOrCreatePersonalChat(Long user1, Long user2) {
        // Ключ "minId:maxId"
        String hashKey = (user1 < user2) ? user1 + ":" + user2 : user2 + ":" + user1;

        return groupRepository.findByDmHashKey(hashKey)
                .orElseGet(() -> {
                    try {
                        log.info("Creating new personal chat for hash: {}", hashKey);
                        Group chat = Group.builder()
                                .type(GroupType.PERSONAL)
                                .dmHashKey(hashKey)
                                .features(new GroupFeatures())
                                .build();

                        Group saved = groupRepository.saveAndFlush(chat);
                        channelService.createChannel(saved, "general");

                        addMemberToGroup(saved, user1);
                        addMemberToGroup(saved, user2);

                        return saved;
                    } catch (DataIntegrityViolationException _) {
                        // Кто-то создал раньше — просто находим
                        return groupRepository.findByDmHashKey(hashKey)
                                .orElseThrow(() -> new IllegalStateException("Concurrent creation failed"));
                    }
                });
    }

    @Transactional
    public void createChat(Long ownerId, GroupRequest request) {
        switch (request.type()) {
            case GROUP -> createGroup(ownerId, request);
            case SERVER -> createServer(ownerId, request.name());
            default -> throw new IllegalArgumentException("Invalid type: " + request.type());
        }
    }

    private Group createGroup(Long ownerId, GroupRequest req) {
        Group group = Group.builder()
                .name(req.name())
                .ownerId(ownerId)
                .type(GroupType.GROUP)
                .features(new GroupFeatures()) // Группа без фич
                .build();

        Group saved = groupRepository.save(group);

        channelService.createChannel(saved, "general");
        addMemberToGroup(saved, ownerId);

        if (req.initialMembers() != null) {
            req.initialMembers().stream()
                    .filter(memberId -> !memberId.equals(ownerId))
                    .distinct()
                    .forEach(memberId -> addMemberToGroup(saved, memberId));
        }

        return saved;
    }

    private Group createServer(Long ownerId, String name) {
        GroupFeatures features = new GroupFeatures();
        features.setRolesEnabled(true); // Кастом роли

        Group server = Group.builder()
                .name(name)
                .ownerId(ownerId)
                .type(GroupType.SERVER)
                .features(features)
                .build();

        Group saved = groupRepository.save(server);
        channelService.createChannel(saved, "general");
        addMemberToGroup(saved, ownerId);

        return saved;
    }

    private void addMemberToGroup(Group group, Long userId) {
        boolean alreadyMember = groupMemberRepository.existsByGroupAndUserId(group, userId);
        if (alreadyMember) {
            log.warn("User {} is already a member of group {}", userId, group.getId());
            return;
        }
        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUserId(userId);
        member.setPermissions(0L); // Заглушка
        groupMemberRepository.save(member);
    }
}
