package com.kokorono_note.kokorono_note.controller;

import com.kokorono_note.kokorono_note.dto.request.WorkspaceJoinRequest;
import com.kokorono_note.kokorono_note.dto.response.InviteResponse;
import com.kokorono_note.kokorono_note.dto.response.WorkspaceResponse;
import com.kokorono_note.kokorono_note.service.WorkspaceInviteService;
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
public class InviteController {

    private final WorkspaceInviteService workspaceInviteService;

    // 招待コード発行
    @PostMapping("/{workspaceId}/invites")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse createInvite(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return workspaceInviteService.createInvite(accountId, workspaceId);
    }

    // 招待コードによる参加
    @PostMapping("/join")
    public WorkspaceResponse joinWorkspace(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody WorkspaceJoinRequest request
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return workspaceInviteService.joinWorkspace(
                accountId, request.code()
        );
    }
}
