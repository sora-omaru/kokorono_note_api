package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.entity.WorkspaceEntity;
import com.kokorono_note.kokorono_note.repository.AccountRepository;
import com.kokorono_note.kokorono_note.repository.WorkspaceAccountRepository;
import com.kokorono_note.kokorono_note.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkspaceServiceTests {
    private final WorkspaceRepository workspaceRepository = mock(WorkspaceRepository.class);
    private final WorkspaceService service = new WorkspaceService(
            mock(AccountRepository.class), workspaceRepository, mock(WorkspaceAccountRepository.class));

    @Test
    void memberCanDeleteWorkspace() {
        UUID accountId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        WorkspaceEntity workspace = new WorkspaceEntity();
        workspace.setId(workspaceId);
        when(workspaceRepository.findByIdAndAccountId(workspaceId, accountId))
                .thenReturn(Optional.of(workspace));

        service.deleteWorkspace(accountId, workspaceId);

        verify(workspaceRepository).delete(workspace);
    }

    @Test
    void missingOrInaccessibleWorkspaceIsNotDeleted() {
        UUID accountId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        when(workspaceRepository.findByIdAndAccountId(workspaceId, accountId))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.deleteWorkspace(accountId, workspaceId));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(workspaceRepository, never()).delete(any(WorkspaceEntity.class));
    }
}
