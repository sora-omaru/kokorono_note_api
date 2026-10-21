package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.dto.response.EventResponse;
import com.kokorono_note.kokorono_note.entity.EventEntity;
import com.kokorono_note.kokorono_note.entity.WorkspaceEntity;
import com.kokorono_note.kokorono_note.repository.EventRepository;
import com.kokorono_note.kokorono_note.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServiceTests {
    private final WorkspaceRepository workspaces = mock(WorkspaceRepository.class);
    private final EventRepository events = mock(EventRepository.class);
    private final EventService service = new EventService(workspaces, events);
    private final UUID accountId = UUID.randomUUID();
    private final UUID workspaceId = UUID.randomUUID();

    @Test
    void listsEventsInRepositoryOrderAndReturnsEmptyListWhenNoneExist() {
        allowWorkspace();
        EventEntity first = event(2L, "旅行");
        EventEntity second = event(1L, "引っ越し");
        when(events.findAllByWorkspaceIdOrderByCreatedAtAscIdAsc(workspaceId))
                .thenReturn(List.of(first, second)).thenReturn(List.of());

        assertEquals(List.of(new EventResponse(2L, "旅行"), new EventResponse(1L, "引っ越し")),
                service.getEvents(accountId, workspaceId));
        assertEquals(List.of(), service.getEvents(accountId, workspaceId));
    }

    @Test
    void getsEventWithinSpecifiedWorkspace() {
        allowWorkspace();
        when(events.findByIdAndWorkspaceId(1L, workspaceId)).thenReturn(Optional.of(event(1L, "旅行")));
        assertEquals(new EventResponse(1L, "旅行"), service.getEventById(accountId, workspaceId, 1L));
    }

    @Test
    void rejectsBothReadsWhenWorkspaceIsMissingOrAccountIsNotAMember() {
        when(workspaces.findByIdAndAccountId(workspaceId, accountId)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service.getEvents(accountId, workspaceId)).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service.getEventById(accountId, workspaceId, 1L)).getStatusCode());
        verifyNoInteractions(events);
    }

    @Test
    void returnsNotFoundWhenEventDoesNotBelongToWorkspaceOrDoesNotExist() {
        allowWorkspace();
        when(events.findByIdAndWorkspaceId(1L, workspaceId)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service.getEventById(accountId, workspaceId, 1L)).getStatusCode());
    }

    private void allowWorkspace() {
        when(workspaces.findByIdAndAccountId(workspaceId, accountId))
                .thenReturn(Optional.of(new WorkspaceEntity()));
    }

    private EventEntity event(Long id, String name) {
        EventEntity event = new EventEntity();
        event.setId(id);
        event.setName(name);
        return event;
    }
}
