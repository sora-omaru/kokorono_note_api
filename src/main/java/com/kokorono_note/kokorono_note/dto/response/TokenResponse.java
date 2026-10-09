package com.kokorono_note.kokorono_note.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
