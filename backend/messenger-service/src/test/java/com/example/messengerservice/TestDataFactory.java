package com.example.messengerservice;

import com.example.messengerservice.entity.enums.GroupFeatures;
import com.example.messengerservice.entity.enums.GroupType;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.entity.groups.TextChannel;
import com.example.messengerservice.repository.groups.GroupMemberRepository;
import com.example.messengerservice.repository.groups.GroupRepository;
import com.example.messengerservice.repository.groups.TextChannelRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestDataFactory {

    private final GroupRepository groupRepository;
    private final TextChannelRepository channelRepository;
    private final GroupMemberRepository memberRepository;

    public TestDataFactory(GroupRepository groupRepository,
                           TextChannelRepository channelRepository,
                           GroupMemberRepository memberRepository) {
        this.groupRepository = groupRepository;
        this.channelRepository = channelRepository;
        this.memberRepository = memberRepository;
    }

    public Group savedGroup(Long ownerId) {
        return groupRepository.save(
                Group.builder()
                        .type(GroupType.GROUP)
                        .name("test-group")
                        .ownerId(ownerId)
                        .features(new GroupFeatures())
                        .build()
        );
    }

    public TextChannel savedChannel(Group group) {
        return channelRepository.save(
                TextChannel.builder()
                        .group(group)
                        .name("general")
                        .build()
        );
    }

    @Transactional
    public void addMember(Group group, Long userId) {
        memberRepository.insertIgnore(group.getId(), userId, 0L);
    }
}