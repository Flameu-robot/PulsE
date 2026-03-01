package com.example.identityservice.repository;

import com.example.identityservice.entity.OAuth2Account;
import com.example.identityservice.entity.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuth2Repository extends JpaRepository<OAuth2Account, Long> {

    Optional<OAuth2Account> findByProviderAndProviderId(OAuthProvider provider, String providerId);
}