package com.kokorono_note.kokorono_note.controller;

import com.kokorono_note.kokorono_note.dto.response.InviteResponse;
import com.kokorono_note.kokorono_note.service.WorkspaceInviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/invites")
@RequiredArgsConstructor
public class InviteController {

    private final WorkspaceInviteService workspaceInviteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse createInvite(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID workspaceId
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return workspaceInviteService.createInvite(accountId, workspaceId);
    }
}
