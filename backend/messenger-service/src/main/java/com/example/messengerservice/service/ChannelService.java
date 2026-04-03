package com.example.messengerservice.service;

import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.entity.groups.TextChannel;
import com.example.messengerservice.repository.groups.TextChannelRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

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

        if (group.getChannels() == null) {
            group.setChannels(new ArrayList<>());
        }
        group.getChannels().add(channel);

        return channelRepository.save(channel);
    }
}
