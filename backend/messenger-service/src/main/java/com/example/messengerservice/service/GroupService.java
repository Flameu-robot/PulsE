package com.example.messengerservice.service;

import com.example.messengerservice.dto.request.GroupRequest;
import com.example.messengerservice.dto.response.GroupResponse;
import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.repository.groups.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupCreationService groupCreationService;

    public Group getOrCreatePersonalChat(Long user1, Long user2) {
        // Ключ "minId:maxId"
        String hashKey = (user1 < user2) ? user1 + ":" + user2 : user2 + ":" + user1;

        return groupRepository.findByDmHashKey(hashKey)
                .orElseGet(() -> groupCreationService.createPersonalChat(hashKey, user1, user2));
    }

    public GroupResponse createChat(Long ownerId, GroupRequest request) {
        Group group;
        switch (request.type()) {
            case GROUP -> group = groupCreationService.createGroup(ownerId, request);
            case SERVER -> group = groupCreationService.createServer(ownerId, request.name());
            default -> throw new IllegalArgumentException("Invalid type: " + request.type());
        }

        return GroupResponse.from(group);
    }
}
