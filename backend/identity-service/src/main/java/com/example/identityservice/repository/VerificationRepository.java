package com.example.identityservice.repository;

import com.example.identityservice.entity.VerificationToken;
import com.example.identityservice.entity.enums.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserIdAndType(Long userId, VerificationType type);
}
