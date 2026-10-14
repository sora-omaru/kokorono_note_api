package com.kokorono_note.kokorono_note.controller;


import com.kokorono_note.kokorono_note.dto.request.WorkspaceCreateRequest;
import com.kokorono_note.kokorono_note.dto.response.WorkspaceResponse;
import com.kokorono_note.kokorono_note.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkspaceResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody WorkspaceCreateRequest request
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());

        return workspaceService.create(accountId, request);
    }
}
