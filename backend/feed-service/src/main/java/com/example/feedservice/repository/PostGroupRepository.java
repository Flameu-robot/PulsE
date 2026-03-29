package com.example.feedservice.repository;

import com.example.feedservice.entity.PostGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostGroupRepository extends JpaRepository<PostGroup, Long> {

    List<PostGroup> findByPostId(Long postId);

    List<PostGroup> findByGroupId(Long groupId);

    void deleteByPostIdAndGroupId(Long postId, Long groupId);

    boolean existsByPostIdAndGroupId(Long postId, Long groupId);

    @Query("SELECT DISTINCT pg.post.id FROM PostGroup pg WHERE pg.groupId IN :groupIds")
    List<Long> findPostIdsByGroupIds(@Param("groupIds") List<Long> groupIds);
}
