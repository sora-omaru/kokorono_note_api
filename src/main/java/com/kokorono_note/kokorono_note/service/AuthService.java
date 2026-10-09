package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.dto.response.TokenResponse;

public interface AuthService {
    TokenResponse exchangeCodeForAccessToken(String code);
}
