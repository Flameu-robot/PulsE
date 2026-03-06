package com.example.identityservice.repository;

import com.example.identityservice.entity.WebAuthnCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WebAuthRepository extends JpaRepository<WebAuthnCredential, Long> {

    List<WebAuthnCredential> findAllByUserId(Long userId);

    Optional<WebAuthnCredential> findByCredentialId(byte[] credentialId);
}
