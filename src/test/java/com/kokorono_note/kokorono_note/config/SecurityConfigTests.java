package com.kokorono_note.kokorono_note.config;

import com.kokorono_note.kokorono_note.entity.AccountEntity;
import com.kokorono_note.kokorono_note.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTests {

    private static KeyPair keyPair;
    private static KeyPair otherKeyPair;
    private final SecurityConfig config = new SecurityConfig(null);

    @BeforeAll
    static void generateTestKeys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
        otherKeyPair = generator.generateKeyPair();
    }

    @Test
    void loadsKeysAndSignsAndVerifiesAccessToken() {
        KeyPair loaded = config.jwtKeyPair(
                encode(keyPair.getPrivate().getEncoded()), encode(keyPair.getPublic().getEncoded()));
        AccountEntity account = new AccountEntity();
        account.setId(UUID.randomUUID());

        String token = new JwtServiceImpl(config.jwtEncoder(loaded)).generateAccessToken(account);
        var jwt = config.jwtDecoder(loaded).decode(token);

        assertEquals(account.getId().toString(), jwt.getSubject());
        assertEquals("RS256", jwt.getHeaders().get("alg"));
        assertEquals(900, jwt.getExpiresAt().getEpochSecond() - jwt.getIssuedAt().getEpochSecond());
        assertThrows(JwtException.class, () -> config.jwtDecoder(otherKeyPair).decode(token));
    }

    @Test
    void rejectsMismatchedKeys() {
        assertThrows(IllegalStateException.class, () -> config.jwtKeyPair(
                encode(keyPair.getPrivate().getEncoded()), encode(otherKeyPair.getPublic().getEncoded())));
    }

    @Test
    void rejectsEmptyOrMalformedKeysWithoutExposingValues() {
        assertThrows(IllegalStateException.class, () -> config.jwtKeyPair("", ""));
        String invalidKey = "invalid-secret-value";
        var exception = assertThrows(IllegalStateException.class,
                () -> config.jwtKeyPair(invalidKey, encode(keyPair.getPublic().getEncoded())));
        assertFalse(exception.getMessage().contains(invalidKey));
        assertNull(exception.getCause());
    }

    @Test
    void rejectsKeysSmallerThan2048Bits() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        KeyPair smallKeyPair = generator.generateKeyPair();
        assertThrows(IllegalStateException.class, () -> config.jwtKeyPair(
                encode(smallKeyPair.getPrivate().getEncoded()), encode(smallKeyPair.getPublic().getEncoded())));
    }

    private static String encode(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }
}
