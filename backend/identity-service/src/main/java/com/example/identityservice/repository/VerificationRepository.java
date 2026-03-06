package com.example.identityservice.repository;

import com.example.identityservice.entity.VerificationToken;
import com.example.identityservice.entity.enums.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VerificationRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserIdAndType(Long userId, VerificationType type);

    // Для корректного удаления аккаунта
    @Modifying
    @Query("DELETE FROM VerificationToken v WHERE v.user.id = :userId")
    void deleteAllByUserId(Long userId);
}
