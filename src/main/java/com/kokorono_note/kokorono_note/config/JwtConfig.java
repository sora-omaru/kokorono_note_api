package com.kokorono_note.kokorono_note.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public KeyPair jwtKeyPair(
            @Value("${app.jwt.private-key}") String privateKey,
            @Value("${app.jwt.public-key}") String publicKey
    ) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            RSAPrivateKey rsaPrivateKey =
                    (RSAPrivateKey) keyFactory.generatePrivate(
                            new PKCS8EncodedKeySpec(
                                    Base64.getDecoder().decode(privateKey)
                            )
                    );

            RSAPublicKey rsaPublicKey =
                    (RSAPublicKey) keyFactory.generatePublic(
                            new X509EncodedKeySpec(
                                    Base64.getDecoder().decode(publicKey)
                            )
                    );

            if (rsaPublicKey.getModulus().bitLength() < 2048
                    || !rsaPublicKey.getModulus()
                    .equals(rsaPrivateKey.getModulus())) {
                throw new IllegalArgumentException("Invalid RSA key pair");
            }

            return new KeyPair(rsaPublicKey, rsaPrivateKey);

        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Invalid JWT RSA key configuration"
            );
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(KeyPair jwtKeyPair) {
        return NimbusJwtEncoder
                .withKeyPair(
                        (RSAPublicKey) jwtKeyPair.getPublic(),
                        (RSAPrivateKey) jwtKeyPair.getPrivate()
                )
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyPair jwtKeyPair) {
        return NimbusJwtDecoder
                .withPublicKey(
                        (RSAPublicKey) jwtKeyPair.getPublic()
                )
                .build();
    }
}
