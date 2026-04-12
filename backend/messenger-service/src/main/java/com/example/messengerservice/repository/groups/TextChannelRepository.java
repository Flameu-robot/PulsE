package com.example.messengerservice.repository.groups;

import com.example.messengerservice.entity.groups.TextChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TextChannelRepository extends JpaRepository<TextChannel, Long> {
    int countByGroupId(Long id);

    Optional<TextChannel> findByIdAndGroupId(Long id, Long groupId);

    Optional<TextChannel> findFirstByGroupId(Long id); // Только для нахождения канала в личном чате
}
