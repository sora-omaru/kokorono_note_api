package com.kokorono_note.kokorono_note.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
        @NotBlank
        @Size(max = 100)
        String name
) {
}
