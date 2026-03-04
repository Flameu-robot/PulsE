package com.example.identityservice.repository;

import com.example.identityservice.entity.User;
import com.example.identityservice.entity.WebAuthnCredential;
import com.example.identityservice.entity.enums.Transport;
import com.example.identityservice.entity.enums.UserRole;
import com.example.identityservice.entity.enums.UserStatus;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebAuthnCredentialRepositoryTest {

    @Mock
    private WebAuthRepository webAuthRepository;

    @Mock
    private UserRepository userRepository;

    private WebAuthnCredentialRepository credentialRepository;

    private User testUser;
    private WebAuthnCredential testCredential;

    @BeforeEach
    void setUp() {
        credentialRepository = new WebAuthnCredentialRepository(webAuthRepository, userRepository);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        testCredential = WebAuthnCredential.builder()
                .id(10L)
                .user(testUser)
                .credentialId(new byte[]{1, 2, 3, 4})
                .publicKey(new byte[]{5, 6, 7, 8})
                .signCount(5L)
                .transports(Set.of(Transport.USB))
                .build();
    }

    @Nested
    @DisplayName("getCredentialIdsForUsername")
    class GetCredentialIds {

        @Test
        @DisplayName("should return credential descriptors for existing user")
        void shouldReturnCredentials() {
            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));
            when(webAuthRepository.findAllByUserId(1L))
                    .thenReturn(List.of(testCredential));

            Set<PublicKeyCredentialDescriptor> result =
                    credentialRepository.getCredentialIdsForUsername("testuser");

            assertThat(result).hasSize(1);

            PublicKeyCredentialDescriptor descriptor = result.iterator().next();
            assertThat(descriptor.getId().getBytes()).isEqualTo(new byte[]{1, 2, 3, 4});
        }

        @Test
        @DisplayName("should return empty set for unknown user")
        void shouldReturnEmptyForUnknownUser() {
            when(userRepository.findByUsername("unknown"))
                    .thenReturn(Optional.empty());

            Set<PublicKeyCredentialDescriptor> result =
                    credentialRepository.getCredentialIdsForUsername("unknown");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUsernameForUserHandle")
    class GetUsernameForUserHandle {

        @Test
        @DisplayName("should return username for valid userHandle")
        void shouldReturnUsername() {
            ByteArray userHandle = new ByteArray(
                    WebAuthnCredentialRepository.longToBytes(1L)
            );

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            Optional<String> result =
                    credentialRepository.getUsernameForUserHandle(userHandle);

            assertThat(result).contains("testuser");
        }

        @Test
        @DisplayName("should return empty for unknown userHandle")
        void shouldReturnEmptyForUnknown() {
            ByteArray userHandle = new ByteArray(
                    WebAuthnCredentialRepository.longToBytes(999L)
            );

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<String> result =
                    credentialRepository.getUsernameForUserHandle(userHandle);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserHandleForUsername")
    class GetUserHandleForUsername {

        @Test
        @DisplayName("should return userHandle for existing user")
        void shouldReturnUserHandle() {
            when(userRepository.findByUsername("testuser"))
                    .thenReturn(Optional.of(testUser));

            Optional<ByteArray> result =
                    credentialRepository.getUserHandleForUsername("testuser");

            assertThat(result).isPresent();

            long userId = WebAuthnCredentialRepository.bytesToLong(
                    result.get().getBytes()
            );
            assertThat(userId).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("lookup")
    class Lookup {

        @Test
        @DisplayName("should return registered credential")
        void shouldReturnCredential() {
            ByteArray credId = new ByteArray(new byte[]{1, 2, 3, 4});
            ByteArray userHandle = new ByteArray(
                    WebAuthnCredentialRepository.longToBytes(1L)
            );

            when(webAuthRepository.findByCredentialId(new byte[]{1, 2, 3, 4}))
                    .thenReturn(Optional.of(testCredential));

            Optional<RegisteredCredential> result =
                    credentialRepository.lookup(credId, userHandle);

            assertThat(result).isPresent();
            assertThat(result.get().getCredentialId().getBytes())
                    .isEqualTo(new byte[]{1, 2, 3, 4});
            assertThat(result.get().getSignatureCount()).isEqualTo(5L);
        }

        @Test
        @DisplayName("should filter by userHandle when provided")
        void shouldFilterByUserHandle() {
            ByteArray credId = new ByteArray(new byte[]{1, 2, 3, 4});
            ByteArray wrongHandle = new ByteArray(
                    WebAuthnCredentialRepository.longToBytes(999L)
            );

            when(webAuthRepository.findByCredentialId(new byte[]{1, 2, 3, 4}))
                    .thenReturn(Optional.of(testCredential));

            Optional<RegisteredCredential> result =
                    credentialRepository.lookup(credId, wrongHandle);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty for unknown credential")
        void shouldReturnEmptyForUnknown() {
            ByteArray credId = new ByteArray(new byte[]{9, 9, 9});

            when(webAuthRepository.findByCredentialId(new byte[]{9, 9, 9}))
                    .thenReturn(Optional.empty());

            Optional<RegisteredCredential> result =
                    credentialRepository.lookup(credId, null);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("lookupAll")
    class LookupAll {

        @Test
        @DisplayName("should return set with credential")
        void shouldReturnSet() {
            ByteArray credId = new ByteArray(new byte[]{1, 2, 3, 4});

            when(webAuthRepository.findByCredentialId(new byte[]{1, 2, 3, 4}))
                    .thenReturn(Optional.of(testCredential));

            Set<RegisteredCredential> result =
                    credentialRepository.lookupAll(credId);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty set for unknown credential")
        void shouldReturnEmptySet() {
            ByteArray credId = new ByteArray(new byte[]{9, 9, 9});

            when(webAuthRepository.findByCredentialId(new byte[]{9, 9, 9}))
                    .thenReturn(Optional.empty());

            Set<RegisteredCredential> result =
                    credentialRepository.lookupAll(credId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("byte conversion helpers")
    class ByteConversion {

        @Test
        @DisplayName("should convert long to bytes and back")
        void shouldConvertRoundTrip() {
            long original = 123456789L;
            byte[] bytes = WebAuthnCredentialRepository.longToBytes(original);
            long result = WebAuthnCredentialRepository.bytesToLong(bytes);

            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("should handle edge case values")
        void shouldHandleEdgeCases() {
            assertThat(WebAuthnCredentialRepository.bytesToLong(
                    WebAuthnCredentialRepository.longToBytes(0L)
            )).isEqualTo(0L);

            assertThat(WebAuthnCredentialRepository.bytesToLong(
                    WebAuthnCredentialRepository.longToBytes(Long.MAX_VALUE)
            )).isEqualTo(Long.MAX_VALUE);
        }
    }
}
