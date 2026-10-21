package com.kokorono_note.kokorono_note.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteUpdateRequest(
        @NotBlank
        @Size(max = 255)
        String content
) {
}
