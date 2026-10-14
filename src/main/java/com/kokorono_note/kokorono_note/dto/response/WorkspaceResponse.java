package com.kokorono_note.kokorono_note.dto.response;

import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        String name
) {
}
