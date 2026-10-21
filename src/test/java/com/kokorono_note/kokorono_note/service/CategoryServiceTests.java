package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.dto.request.CategoryCreateRequest;
import com.kokorono_note.kokorono_note.dto.response.CategoryResponse;
import com.kokorono_note.kokorono_note.entity.CategoryEntity;
import com.kokorono_note.kokorono_note.entity.EventEntity;
import com.kokorono_note.kokorono_note.entity.WorkspaceEntity;
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

class CategoryServiceTests {
    private final WorkspaceRepository workspaces = mock(WorkspaceRepository.class);
    private final EventRepository events = mock(EventRepository.class);
    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final CategoryService service = new CategoryService(workspaces, events, categories);
    private final UUID accountId = UUID.randomUUID();
    private final UUID workspaceId = UUID.randomUUID();
    private final Long eventId = 10L;
    private final EventEntity event = new EventEntity();

    @Test
    void createsCategoryWithTrimmedNameAndParentEvent() {
        allowEvent();
        when(categories.save(any(CategoryEntity.class))).thenAnswer(invocation -> {
            CategoryEntity category = invocation.getArgument(0);
            assertSame(event, category.getEvent());
            assertEquals("持ち物", category.getName());
            category.setId(1L);
            return category;
        });
        assertEquals(new CategoryResponse(1L, "持ち物"),
                service.createCategory(accountId, workspaceId, eventId, new CategoryCreateRequest(" 持ち物 ")));
    }

    @Test
    void listsCategoriesAndReturnsEmptyListWhenNoneExist() {
        allowEvent();
        when(categories.findAllByEventIdOrderByCreatedAtAscIdAsc(eventId))
                .thenReturn(List.of(category(2L), category(1L))).thenReturn(List.of());
        assertEquals(List.of(new CategoryResponse(2L, "持ち物"), new CategoryResponse(1L, "持ち物")),
                service.getCategories(accountId, workspaceId, eventId));
        assertEquals(List.of(), service.getCategories(accountId, workspaceId, eventId));
    }

    @Test
    void getsAndDeletesCategoryWithinEvent() {
        allowEvent();
        CategoryEntity category = category(1L);
        when(categories.findByIdAndEventId(1L, eventId)).thenReturn(Optional.of(category));
        assertEquals(new CategoryResponse(1L, "持ち物"),
                service.getCategoryById(accountId, workspaceId, eventId, 1L));
        service.deleteCategory(accountId, workspaceId, eventId, 1L);
        verify(categories).delete(category);
    }

    @Test
    void rejectsAllOperationsWhenAccountIsNotInWorkspace() {
        when(workspaces.findByIdAndAccountId(workspaceId, accountId)).thenReturn(Optional.empty());
        assertAllOperationsNotFound();
        verifyNoInteractions(events, categories);
    }

    @Test
    void rejectsAllOperationsWhenEventIsMissingOrInAnotherWorkspace() {
        when(workspaces.findByIdAndAccountId(workspaceId, accountId))
                .thenReturn(Optional.of(new WorkspaceEntity()));
        when(events.findByIdAndWorkspaceId(eventId, workspaceId)).thenReturn(Optional.empty());
        assertAllOperationsNotFound();
        verifyNoInteractions(categories);
    }

    @Test
    void rejectsGetAndDeleteWhenCategoryIsMissingOrInAnotherEvent() {
        allowEvent();
        when(categories.findByIdAndEventId(1L, eventId)).thenReturn(Optional.empty());
        assertNotFound(() -> service.getCategoryById(accountId, workspaceId, eventId, 1L));
        assertNotFound(() -> service.deleteCategory(accountId, workspaceId, eventId, 1L));
        verify(categories, never()).delete(any(CategoryEntity.class));
    }

    private void assertAllOperationsNotFound() {
        assertNotFound(() -> service.createCategory(accountId, workspaceId, eventId,
                new CategoryCreateRequest("持ち物")));
        assertNotFound(() -> service.getCategories(accountId, workspaceId, eventId));
        assertNotFound(() -> service.getCategoryById(accountId, workspaceId, eventId, 1L));
        assertNotFound(() -> service.deleteCategory(accountId, workspaceId, eventId, 1L));
    }

    private void assertNotFound(org.junit.jupiter.api.function.Executable action) {
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class, action).getStatusCode());
    }

    private void allowEvent() {
        when(workspaces.findByIdAndAccountId(workspaceId, accountId))
                .thenReturn(Optional.of(new WorkspaceEntity()));
        when(events.findByIdAndWorkspaceId(eventId, workspaceId)).thenReturn(Optional.of(event));
    }

    private CategoryEntity category(Long id) {
        CategoryEntity category = new CategoryEntity();
        category.setId(id);
        category.setName("持ち物");
        return category;
    }
}
