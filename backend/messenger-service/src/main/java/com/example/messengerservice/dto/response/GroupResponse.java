package com.example.messengerservice.dto.response;

import com.example.messengerservice.entity.enums.GroupType;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.entity.groups.GroupMember;
import com.example.messengerservice.entity.groups.TextChannel;

import java.time.OffsetDateTime;
import java.util.List;

public record GroupResponse(
        Long groupId,
        String name,
        GroupType type,
        Long ownerId,
        List<ChannelShortDto> channels,
        List<MemberShortDto> members,
        OffsetDateTime createdAt
) {
    public record ChannelShortDto(
            Long channelId,
            String name
    ) {
        public static ChannelShortDto from(TextChannel channel) {
            return new ChannelShortDto(
                    channel.getId(),
                    channel.getName()
            );
        }
    }

    public record MemberShortDto(
            Long userId,
            OffsetDateTime joinedAt
    ) {
        public static MemberShortDto from(GroupMember member) {
            return new MemberShortDto(
                    member.getUserId(),
                    member.getJoinedAt()
            );
        }
    }

    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getType(),
                group.getOwnerId(),
                group.getChannels().stream()
                        .map(ChannelShortDto::from)
                        .toList(),
                group.getMembers().stream()
                        .map(MemberShortDto::from)
                        .toList(),
                group.getCreatedAt()
        );
    }
}