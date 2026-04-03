package com.example.messengerservice.service;

import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.entity.groups.TextChannel;
import com.example.messengerservice.repository.groups.TextChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final TextChannelRepository channelRepository;

    @Transactional
    public TextChannel createChannel(Group group, String name) {

        TextChannel channel = TextChannel.builder()
                .group(group)
                .name(name)
                .build();

        group.getChannels().add(channel);

        return channelRepository.save(channel);
    }
}
