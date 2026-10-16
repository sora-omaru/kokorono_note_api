package com.kokorono_note.kokorono_note.dto.response;

import java.time.OffsetDateTime;

public record InviteResponse(
        String code,
        OffsetDateTime expiresAt
) {
}
