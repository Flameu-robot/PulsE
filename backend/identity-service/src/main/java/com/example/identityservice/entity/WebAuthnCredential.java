package com.example.identityservice.entity;

import com.example.identityservice.entity.enums.Transport;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "webauth",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_webauthn_credential_id", columnNames = "credential_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebAuthnCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_webauthn_user"))
    private User user;

    @Column(name = "credential_id", nullable = false, columnDefinition = "bytea")
    private byte[] credentialId;

    @Column(name = "public_key", nullable = false, columnDefinition = "bytea")
    private byte[] publicKey;

    @Column(name = "sign_count", nullable = false)
    @Builder.Default
    private long signCount = 0L;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "webauth_transports",
            joinColumns = @JoinColumn(name = "credential_id"),
            foreignKey = @ForeignKey(name = "fk_webauth_transports_credential")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "transport", length = 20)
    @Builder.Default
    private Set<Transport> transports = new LinkedHashSet<>();

    private UUID aaguid;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebAuthnCredential that = (WebAuthnCredential) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
