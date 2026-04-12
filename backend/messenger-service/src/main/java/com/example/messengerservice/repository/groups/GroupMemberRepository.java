package com.example.messengerservice.repository.groups;

import com.example.messengerservice.entity.groups.Group;
import com.example.messengerservice.entity.groups.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    Collection<Object> findAllByGroup(Group chat);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    INSERT INTO group_members (group_id, user_id, permissions)
    VALUES (:groupId, :userId, :permissions)
    ON CONFLICT (group_id, user_id) DO NOTHING
    """, nativeQuery = true)
    int insertIgnore(@Param("groupId") Long groupId,
                     @Param("userId") Long userId,
                     @Param("permissions") Long permissions);

    int countByGroupId(Long id);

    boolean existsByUserIdAndGroupId(Long memberId, Long groupId);
}
