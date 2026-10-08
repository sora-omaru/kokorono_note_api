package com.kokorono_note.kokorono_note.service;

public interface AuthService {
    String exchangeCodeForAccessToken(String code);
}
