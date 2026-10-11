package com.kokorono_note.kokorono_note.service.impl;

import com.kokorono_note.kokorono_note.dto.response.RefreshTokenRotationResult;
import com.kokorono_note.kokorono_note.dto.response.TokenResponse;
import com.kokorono_note.kokorono_note.entity.AccountEntity;
import com.kokorono_note.kokorono_note.repository.AccountRepository;
import com.kokorono_note.kokorono_note.service.AuthService;
import com.kokorono_note.kokorono_note.service.JwtService;
import com.kokorono_note.kokorono_note.service.RefreshTokenService;
import com.kokorono_note.kokorono_note.service.oauth.TemporaryAuthCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final TemporaryAuthCodeService temporaryAuthCodeService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    // 認証処理をまとめる
    @Override
    public TokenResponse exchangeCodeForAccessToken(String code) {
        //一時認証コードを消費してaccountIdを取得する
        UUID accountId = temporaryAuthCodeService.consume(code);

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("Account Not Found"));

        //JwtServiceにRefreshTokenの作成依頼
        String accessToken = jwtService.generateAccessToken(account);

        String refreshToken = refreshTokenService.generateRefreshToken(account);

        //二つのトークンを返却
        return new TokenResponse(accessToken, refreshToken);
    }

    //新しいaccessTokenと新しいRefreshTokenを作成
    @Override
    public TokenResponse refresh(String rawToken) {
        RefreshTokenRotationResult result = refreshTokenService.rotateRefreshToken(rawToken);

        String accessToken = jwtService.generateAccessToken(result.account());

        return new TokenResponse(accessToken, result.refreshToken());
    }
}
