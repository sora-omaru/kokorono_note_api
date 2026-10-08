package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.entity.AccountEntity;

public interface RefreshTokenService {
    String generateRefreshToken(AccountEntity account);
}
