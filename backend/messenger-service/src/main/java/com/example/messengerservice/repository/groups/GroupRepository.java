package com.example.messengerservice.repository.groups;

import com.example.messengerservice.entity.groups.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
