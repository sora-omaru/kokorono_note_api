package com.kokorono_note.kokorono_note.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record WorkspaceJoinRequest(
        @NotBlank
        @Pattern(regexp = "[A-Z0-9]{6}")
        String code
) {
}
