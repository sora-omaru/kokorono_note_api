package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.dto.response.RefreshTokenRotationResult;
import com.kokorono_note.kokorono_note.entity.AccountEntity;

public interface RefreshTokenService {
    //新しいRefreshTokenを作成し、DBに保存
    String generateRefreshToken(AccountEntity account);

    RefreshTokenRotationResult rotateRefreshToken(String rawToken);

}
