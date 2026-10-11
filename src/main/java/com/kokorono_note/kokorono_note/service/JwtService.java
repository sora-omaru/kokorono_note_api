package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.entity.AccountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public String generateAccessToken(AccountEntity account) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet
                .builder()
                .subject(account.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES)).build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }
}
