package com.example.messengerservice.service;

import com.example.messengerservice.dto.request.GroupRequest;
import com.example.messengerservice.entity.enums.GroupFeatures;
import com.example.messengerservice.entity.enums.GroupType;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.repository.groups.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupCreationService {

    private final GroupRepository groupRepository;
    private final ChannelService channelService;
    private final MemberService memberService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Group createPersonalChat(String hashKey, Long user1, Long user2) {
        try {
            Group chat = Group.builder()
                    .type(GroupType.PERSONAL)
                    .dmHashKey(hashKey)
                    .features(new GroupFeatures()) // TODO
                    .build();

            Group saved = groupRepository.saveAndFlush(chat);
            channelService.createChannel(saved, "general");

            memberService.addMemberToGroup(saved, user1);
            memberService.addMemberToGroup(saved, user2);

            return saved;
        } catch (DataIntegrityViolationException _) {
            return groupRepository.findByDmHashKey(hashKey)
                    .orElseThrow(() -> new IllegalStateException("Concurrent creation failed"));
        }
    }

    @Transactional
    public Group createGroup(Long ownerId, GroupRequest req) {
        Group group = Group.builder()
                .name(req.name())
                .ownerId(ownerId)
                .type(GroupType.GROUP)
                .features(new GroupFeatures()) // TODO
                .build();

        Group saved = groupRepository.save(group);

        channelService.createChannel(saved, "general");
        memberService.addMemberToGroup(saved, ownerId);

        if (req.initialMembers() != null) {
            req.initialMembers().stream()
                    .filter(memberId -> !memberId.equals(ownerId))
                    .distinct()
                    .forEach(memberId -> memberService.addMemberToGroup(saved, memberId));
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
        channelService.createChannel(saved, "general");
        memberService.addMemberToGroup(saved, ownerId);

        return saved;
    }
}
