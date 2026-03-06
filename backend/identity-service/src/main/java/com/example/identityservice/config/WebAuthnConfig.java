package com.example.identityservice.config;

import com.example.identityservice.repository.WebAuthnCredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, WebAuthnProperties.class, OAuth2Properties.class})
public class WebAuthnConfig {

    @Bean
    public RelyingParty relyingParty(
            WebAuthnProperties properties,
            WebAuthnCredentialRepository credentialRepository
    ) {
        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(properties.rpId())
                .name(properties.rpName())
                .build();

        return RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(credentialRepository)
                .origins(Set.of(properties.origin()))
                .build();
    }
}
