package com.example.feedservice.repository;

import com.example.feedservice.entity.PostMusicLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostMusicLinkRepository extends JpaRepository<PostMusicLink, Long> {

    List<PostMusicLink> findByPostId(Long postId);

    @Modifying
    @Query("UPDATE PostMusicLink l SET l.listenCount = l.listenCount + 1 WHERE l.id = :id")
    void incrementListenCount(@Param("id") Long id);
}

