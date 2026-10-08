package com.kokorono_note.kokorono_note.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/auth/token")
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/auth/token").permitAll()
                        .requestMatchers(
                                "/oauth2/authorization/**",
                                "/login/oauth2/code/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults())
                );

        return http.build();
    }

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
                    "Invalid JWT RSA key configuration",
                    exception
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