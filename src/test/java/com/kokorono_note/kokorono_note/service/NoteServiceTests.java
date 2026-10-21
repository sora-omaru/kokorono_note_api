package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.dto.request.NoteCreateRequest;
import com.kokorono_note.kokorono_note.dto.request.NoteUpdateRequest;
import com.kokorono_note.kokorono_note.dto.response.NoteResponse;
import com.kokorono_note.kokorono_note.entity.NoteEntity;
import com.kokorono_note.kokorono_note.entity.CategoryEntity;
import com.kokorono_note.kokorono_note.entity.EventEntity;
import com.kokorono_note.kokorono_note.entity.WorkspaceEntity;
import com.kokorono_note.kokorono_note.repository.NoteRepository;
import com.kokorono_note.kokorono_note.repository.CategoryRepository;
import com.kokorono_note.kokorono_note.repository.EventRepository;
import com.kokorono_note.kokorono_note.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NoteServiceTests {
    private final WorkspaceRepository workspaces = mock(WorkspaceRepository.class);
    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final NoteRepository notes = mock(NoteRepository.class);
    private final EventRepository events = mock(EventRepository.class);
    private final Long eventId = 5L;
    private final NoteService service = new NoteService(workspaces, events, categories, notes);
    private final UUID accountId = UUID.randomUUID();
    private final UUID workspaceId = UUID.randomUUID();
    private final Long categoryId = 10L;
    private final CategoryEntity category = new CategoryEntity();

    @Test
    void createsNoteWithTrimmedContentAndParentCategory() {
        allowCategory();
        when(notes.save(any(NoteEntity.class))).thenAnswer(invocation -> {
            NoteEntity note = invocation.getArgument(0);
            assertSame(category, note.getCategory());
            assertEquals("持ち物", note.getContent());
            note.setId(1L);
            return note;
        });
        assertEquals(new NoteResponse(1L, "持ち物"),
                service.createNote(accountId, workspaceId, eventId, categoryId, new NoteCreateRequest(" 持ち物 ")));
    }

    @Test
    void listsNotesAndReturnsEmptyListWhenNoneExist() {
        allowCategory();
        when(notes.findAllByCategoryIdOrderByCreatedAtAscIdAsc(categoryId))
                .thenReturn(List.of(note(2L), note(1L))).thenReturn(List.of());
        assertEquals(List.of(new NoteResponse(2L, "持ち物"), new NoteResponse(1L, "持ち物")),
                service.getNotes(accountId, workspaceId, eventId, categoryId));
        assertEquals(List.of(), service.getNotes(accountId, workspaceId, eventId, categoryId));
    }

    @Test
    void getsAndDeletesNoteWithinCategory() {
        allowCategory();
        NoteEntity note = note(1L);
        when(notes.findByIdAndCategoryId(1L, categoryId)).thenReturn(Optional.of(note));
        assertEquals(new NoteResponse(1L, "持ち物"),
                service.getNoteById(accountId, workspaceId, eventId, categoryId, 1L));
        service.deleteNote(accountId, workspaceId, eventId, categoryId, 1L);
        verify(notes).delete(note);
    }

    @Test
    void rejectsAllOperationsWhenAccountIsNotInWorkspace() {
        when(workspaces.findByIdAndAccountId(workspaceId, accountId)).thenReturn(Optional.empty());
        assertAllOperationsNotFound();
        verifyNoInteractions(events, categories, notes);
    }

    @Test
    void rejectsAllOperationsWhenCategoryIsMissingOrInAnotherEvent() {
        when(workspaces.findByIdAndAccountId(workspaceId, accountId))
                .thenReturn(Optional.of(new WorkspaceEntity()));
        when(events.findByIdAndWorkspaceId(eventId, workspaceId)).thenReturn(Optional.of(new EventEntity()));
        when(categories.findByIdAndEventId(categoryId, eventId)).thenReturn(Optional.empty());
        assertAllOperationsNotFound();
        verifyNoInteractions(notes);
    }

    @Test
    void rejectsGetUpdateAndDeleteWhenNoteIsMissingOrInAnotherCategory() {
        allowCategory();
        when(notes.findByIdAndCategoryId(1L, categoryId)).thenReturn(Optional.empty());
        assertNotFound(() -> service.getNoteById(accountId, workspaceId, eventId, categoryId, 1L));
        assertNotFound(() -> service.deleteNote(accountId, workspaceId, eventId, categoryId, 1L));
        assertNotFound(() -> service.updateNote(accountId, workspaceId, eventId, categoryId, 1L, new NoteUpdateRequest("更新")));
        verify(notes, never()).delete(any(NoteEntity.class));
    }

    private void assertAllOperationsNotFound() {
        assertNotFound(() -> service.createNote(accountId, workspaceId, eventId, categoryId,
                new NoteCreateRequest("持ち物")));
        assertNotFound(() -> service.getNotes(accountId, workspaceId, eventId, categoryId));
        assertNotFound(() -> service.getNoteById(accountId, workspaceId, eventId, categoryId, 1L));
        assertNotFound(() -> service.deleteNote(accountId, workspaceId, eventId, categoryId, 1L));
        assertNotFound(() -> service.updateNote(accountId, workspaceId, eventId, categoryId, 1L, new NoteUpdateRequest("更新")));
    }

    private void assertNotFound(org.junit.jupiter.api.function.Executable action) {
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class, action).getStatusCode());
    }

    private void allowCategory() {
        when(workspaces.findByIdAndAccountId(workspaceId, accountId))
                .thenReturn(Optional.of(new WorkspaceEntity()));
        when(events.findByIdAndWorkspaceId(eventId, workspaceId)).thenReturn(Optional.of(new EventEntity()));
        when(categories.findByIdAndEventId(categoryId, eventId)).thenReturn(Optional.of(category));
    }

    private NoteEntity note(Long id) {
        NoteEntity note = new NoteEntity();
        note.setId(id);
        note.setContent("持ち物");
        return note;
    }

    @Test
    void updatesOnlyContentAndTrimsWhitespace() {
        allowCategory();
        NoteEntity note = note(1L);
        note.setCategory(category);
        when(notes.findByIdAndCategoryId(1L, categoryId)).thenReturn(Optional.of(note));
        assertEquals(new NoteResponse(1L, "更新"), service.updateNote(accountId, workspaceId,
                eventId, categoryId, 1L, new NoteUpdateRequest(" 更新 ")));
        assertEquals("更新", note.getContent());
        assertSame(category, note.getCategory());
        assertEquals(1L, note.getId());
    }

    @Test
    void rejectsAllOperationsWhenEventIsMissingOrInAnotherWorkspace() {
        when(workspaces.findByIdAndAccountId(workspaceId, accountId))
                .thenReturn(Optional.of(new WorkspaceEntity()));
        assertAllOperationsNotFound();
        verifyNoInteractions(categories, notes);
    }

}
