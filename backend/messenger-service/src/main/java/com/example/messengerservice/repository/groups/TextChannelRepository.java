package com.example.messengerservice.repository.groups;

import com.example.messengerservice.entity.groups.TextChannel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextChannelRepository extends JpaRepository<TextChannel, Long> {
    int countByGroupId(Long id);
}
