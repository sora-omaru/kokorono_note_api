package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.entity.AccountEntity;

public interface JwtService {
    String generateAccessToken(AccountEntity account);
}
