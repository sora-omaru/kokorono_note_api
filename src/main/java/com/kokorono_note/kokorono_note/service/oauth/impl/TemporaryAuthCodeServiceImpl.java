package com.kokorono_note.kokorono_note.service.oauth.impl;

import com.kokorono_note.kokorono_note.service.oauth.TemporaryAuthCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
//フロントエンドへ渡す、一時コード作成クラス
public class TemporaryAuthCodeServiceImpl implements TemporaryAuthCodeService {
    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(2);

    private final SecureRandom secureRandom = new SecureRandom();

    private final Map<String, TemporaryAuthCode> codes =
            new ConcurrentHashMap<>();


    @Override
    public String generate(UUID accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException(
                    "accountId must not be null"
            );
        }
        //今から２分間という期限を作成
        Instant expiresAt = Instant.now().plus(CODE_EXPIRATION);

        //accountId + 有効期限を一つにする
        TemporaryAuthCode temporaryAuthCode = new TemporaryAuthCode(accountId, expiresAt);

        while (true) {
            //一時コード作成
            String code = generateRandomCode();

            TemporaryAuthCode existing =
                    codes.putIfAbsent(
                            code,
                            temporaryAuthCode
                    );
            if (existing == null) {
                return code;
            }
        }

    }

    @Override
    public UUID consume(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Authentication code is required"
            );
        }
        TemporaryAuthCode temporaryAuthCode = codes.remove(code);
        if (temporaryAuthCode == null) {
            throw new IllegalArgumentException(
                    "Invalid authentication code"
            );
        }

        if (temporaryAuthCode.expiresAt()
                .isBefore(Instant.now())) {

            throw new IllegalArgumentException(
                    "Authentication code has expired"
            );
        }

        return temporaryAuthCode.accountId();

    }

    private String generateRandomCode() {
        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }


    private record TemporaryAuthCode(
            UUID accountId,
            Instant expiresAt
    ) {
    }
}
