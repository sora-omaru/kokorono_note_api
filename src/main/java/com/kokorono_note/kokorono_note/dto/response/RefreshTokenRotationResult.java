package com.kokorono_note.kokorono_note.dto.response;

import com.kokorono_note.kokorono_note.entity.AccountEntity;

public record RefreshTokenRotationResult(
        AccountEntity account,
        String refreshToken
) {
}
