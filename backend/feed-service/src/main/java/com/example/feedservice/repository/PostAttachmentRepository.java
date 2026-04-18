package com.example.feedservice.repository;

import com.example.feedservice.entity.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {

    List<PostAttachment> findByPostIdOrderByOrderIndexAsc(Long postId);

    void deleteAllByPostId(Long postId);
}
