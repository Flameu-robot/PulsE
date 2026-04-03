package com.example.messengerservice.repository.groups;

import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.entity.groups.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    boolean existsByGroupAndUserId(Group group, Long userId);
}
