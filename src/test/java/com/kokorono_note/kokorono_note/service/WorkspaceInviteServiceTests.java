package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.entity.*;
import com.kokorono_note.kokorono_note.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WorkspaceInviteServiceTests {
    private final WorkspaceRepository workspaces = mock(WorkspaceRepository.class);
    private final WorkspaceInviteRepository invites = mock(WorkspaceInviteRepository.class);
    private final WorkspaceAccountRepository memberships = mock(WorkspaceAccountRepository.class);
    private final WorkspaceInviteService service = new WorkspaceInviteService(
            workspaces, invites, mock(AccountRepository.class), memberships);

    @Test
    void issuingNewCodeExpiresPreviousCodeAndKeepsNewCodeUsable() {
        UUID accountId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        WorkspaceEntity workspace = new WorkspaceEntity();
        workspace.setId(workspaceId);
        WorkspaceInviteEntity oldInvite = new WorkspaceInviteEntity();
        oldInvite.setWorkspace(workspace);
        oldInvite.setExpiresAt(OffsetDateTime.now().plusMinutes(30));
        when(workspaces.findByIdAndAccountId(workspaceId, accountId)).thenReturn(Optional.of(workspace));
        when(invites.expireByWorkspaceId(eq(workspaceId), any())).thenAnswer(call -> {
            oldInvite.setExpiresAt(call.getArgument(1));
            return 1;
        });
        when(invites.save(any())).thenAnswer(call -> call.getArgument(0));
        when(invites.findByCode("OLD123")).thenReturn(Optional.of(oldInvite));

        var response = service.createInvite(accountId, workspaceId);

        assertTrue(response.expiresAt().isAfter(OffsetDateTime.now()));
        var order = inOrder(workspaces, invites);
        order.verify(workspaces).lockById(workspaceId);
        order.verify(invites).expireByWorkspaceId(eq(workspaceId), any());
        order.verify(invites).save(any());
        var error = assertThrows(ResponseStatusException.class,
                () -> service.joinWorkspace(UUID.randomUUID(), "OLD123"));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(memberships);
    }

    @Test
    void inaccessibleWorkspaceDoesNotInvalidateCodes() {
        assertThrows(ResponseStatusException.class,
                () -> service.createInvite(UUID.randomUUID(), UUID.randomUUID()));
        verifyNoInteractions(invites);
        verify(workspaces, never()).lockById(any());
    }
}
