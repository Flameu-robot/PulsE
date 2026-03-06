package com.example.identityservice.repository;

import com.example.identityservice.entity.OAuth2Account;
import com.example.identityservice.entity.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OAuth2Repository extends JpaRepository<OAuth2Account, Long> {

    Optional<OAuth2Account> findByProviderAndProviderId(OAuthProvider provider, String providerId);

    List<OAuth2Account> findAllByUserId(Long userId);

    long countByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM OAuth2Account a WHERE a.user.id = :userId AND a.provider = :provider")
    void deleteByUserIdAndProvider(Long userId, OAuthProvider provider);
}