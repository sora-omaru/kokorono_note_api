package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.dto.request.*;
import com.kokorono_note.kokorono_note.entity.AccountEntity;
import com.kokorono_note.kokorono_note.repository.AccountRepository;
import com.kokorono_note.kokorono_note.repository.EventRepository;
import com.kokorono_note.kokorono_note.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class HierarchyNameUpdateIntegrationTests {
    @Autowired AccountRepository accounts;
    @Autowired WorkspaceService workspaces;
    @Autowired EventService events;
    @Autowired CategoryService categories;
    @Autowired EventRepository eventRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired EntityManager entityManager;

    @Test
    void persistsTrimmedNamesWithoutChangingCreationOrderOrParents() {
        UUID accountId = account();
        UUID workspaceId = workspaces.create(accountId, new WorkspaceCreateRequest("workspace")).id();
        var first = events.createEvent(accountId, workspaceId, new EventCreateRequest("first"));
        var second = events.createEvent(accountId, workspaceId, new EventCreateRequest("second"));
        var category = categories.createCategory(accountId, workspaceId, first.id(), new CategoryCreateRequest("first category"));
        var secondCategory = categories.createCategory(accountId, workspaceId, first.id(), new CategoryCreateRequest("second category"));
        entityManager.flush();
        entityManager.clear();
        var eventCreatedAt = eventRepository.findById(first.id()).orElseThrow().getCreatedAt();
        var categoryCreatedAt = categoryRepository.findById(category.id()).orElseThrow().getCreatedAt();

        assertEquals("renamed event", events.updateEvent(accountId, workspaceId, first.id(), new EventUpdateRequest(" renamed event ")).name());
        assertEquals("renamed category", categories.updateCategory(accountId, workspaceId, first.id(), category.id(), new CategoryUpdateRequest(" renamed category ")).name());
        entityManager.flush();
        entityManager.clear();

        assertEquals("renamed event", events.getEventById(accountId, workspaceId, first.id()).name());
        assertEquals("renamed category", categories.getCategoryById(accountId, workspaceId, first.id(), category.id()).name());
        assertEquals(List.of(first.id(), second.id()), events.getEvents(accountId, workspaceId).stream().map(item -> item.id()).toList());
        assertEquals(List.of(category.id(), secondCategory.id()), categories.getCategories(accountId, workspaceId, first.id()).stream().map(item -> item.id()).toList());
        var savedEvent = eventRepository.findById(first.id()).orElseThrow();
        var savedCategory = categoryRepository.findById(category.id()).orElseThrow();
        assertTrue(eventCreatedAt.isEqual(savedEvent.getCreatedAt()));
        assertTrue(categoryCreatedAt.isEqual(savedCategory.getCreatedAt()));
        assertEquals(workspaceId, savedEvent.getWorkspace().getId());
        assertEquals(first.id(), savedCategory.getEvent().getId());
    }

    @Test
    void refusesNonMembersMissingEntriesAndIncorrectParentsWithoutChangingData() {
        UUID owner = account(), outsider = account();
        UUID workspaceId = workspaces.create(owner, new WorkspaceCreateRequest("one")).id();
        UUID otherWorkspace = workspaces.create(owner, new WorkspaceCreateRequest("two")).id();
        var event = events.createEvent(owner, workspaceId, new EventCreateRequest("original"));
        var otherEvent = events.createEvent(owner, workspaceId, new EventCreateRequest("another"));
        var category = categories.createCategory(owner, workspaceId, event.id(), new CategoryCreateRequest("original category"));
        assertNotFound(() -> events.updateEvent(outsider, workspaceId, event.id(), new EventUpdateRequest("bad")));
        assertNotFound(() -> events.updateEvent(owner, otherWorkspace, event.id(), new EventUpdateRequest("bad")));
        assertNotFound(() -> events.updateEvent(owner, workspaceId, Long.MAX_VALUE, new EventUpdateRequest("bad")));
        assertNotFound(() -> categories.updateCategory(outsider, workspaceId, event.id(), category.id(), new CategoryUpdateRequest("bad")));
        assertNotFound(() -> categories.updateCategory(owner, otherWorkspace, event.id(), category.id(), new CategoryUpdateRequest("bad")));
        assertNotFound(() -> categories.updateCategory(owner, workspaceId, otherEvent.id(), category.id(), new CategoryUpdateRequest("bad")));
        assertNotFound(() -> categories.updateCategory(owner, workspaceId, event.id(), Long.MAX_VALUE, new CategoryUpdateRequest("bad")));
        assertEquals("original", events.getEventById(owner, workspaceId, event.id()).name());
        assertEquals("original category", categories.getCategoryById(owner, workspaceId, event.id(), category.id()).name());
    }

    private UUID account() {
        var account = new AccountEntity();
        account.setGoogleSub("name-update-" + UUID.randomUUID());
        return accounts.saveAndFlush(account).getId();
    }

    private void assertNotFound(org.junit.jupiter.api.function.Executable action) {
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class, action).getStatusCode());
    }
}
