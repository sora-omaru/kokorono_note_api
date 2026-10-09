package com.kokorono_note.kokorono_note.service.impl;

import com.kokorono_note.kokorono_note.dto.response.RefreshTokenRotationResult;
import com.kokorono_note.kokorono_note.entity.AccountEntity;
import com.kokorono_note.kokorono_note.entity.RefreshTokenEntity;
import com.kokorono_note.kokorono_note.repository.RefreshTokenRepository;
import com.kokorono_note.kokorono_note.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public String generateRefreshToken(AccountEntity account) {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        //URLように加工
        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        //Tokenをhash化する(DB漏洩防止)
        String tokenHash = hashToken(rawToken);

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();

        refreshToken.setAccount(account);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(
                OffsetDateTime.now().plusDays(30)
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    @Override
    public RefreshTokenRotationResult rotateRefreshToken(String rawToken) {
//
        String tokenHash = hashToken(rawToken);


        RefreshTokenEntity refreshToken =
                refreshTokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid refresh Token"));

        // 有効期限の確認
        if (!refreshToken.getExpiresAt().isAfter(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Refresh token has expired");
        }


        // 無効化済みか確認
        if (refreshToken.getRevokedAt() != null) {
            throw new IllegalArgumentException(
                    "Refresh token has already been revoked"
            );
        }

        // 古いRefresh Tokenを無効化
        refreshToken.setRevokedAt(OffsetDateTime.now());

        //新しいRefreshToken発行
        String newRefreshToken = generateRefreshToken(refreshToken.getAccount());


        return new RefreshTokenRotationResult(refreshToken.getAccount(), newRefreshToken);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 is not available", e
            );
        }
    }
}
