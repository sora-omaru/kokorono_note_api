package com.kokorono_note.kokorono_note.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteCreateRequest(
        @NotBlank
        @Size(max = 255)
        String content
) {
}
