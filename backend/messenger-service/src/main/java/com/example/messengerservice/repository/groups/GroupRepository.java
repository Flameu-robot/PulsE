package com.example.messengerservice.repository.groups;

import com.example.messengerservice.entity.groups.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByDmHashKey(String hashKey);
}
