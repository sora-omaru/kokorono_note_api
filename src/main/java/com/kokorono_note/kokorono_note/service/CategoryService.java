package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.dto.request.CategoryCreateRequest;
import com.kokorono_note.kokorono_note.dto.response.CategoryResponse;
import com.kokorono_note.kokorono_note.entity.CategoryEntity;
import com.kokorono_note.kokorono_note.entity.EventEntity;
import com.kokorono_note.kokorono_note.repository.CategoryRepository;
import com.kokorono_note.kokorono_note.repository.EventRepository;
import com.kokorono_note.kokorono_note.repository.WorkspaceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final WorkspaceRepository workspaceRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    // 指定EventにCategoryを作成する。
    @Transactional
    public CategoryResponse createCategory(UUID accountId, UUID workspaceId, Long eventId,
                                           CategoryCreateRequest request) {
        EventEntity event = requireEvent(accountId, workspaceId, eventId);

        CategoryEntity category = new CategoryEntity();
        category.setEvent(event);
        category.setName(request.name().trim());

        CategoryEntity saved = categoryRepository.save(category);

        return new CategoryResponse(saved.getId(), saved.getName());
    }

    // 登録日時順で一覧を取得する。同じ日時の場合はID順とする。
    @Transactional
    public List<CategoryResponse> getCategories(UUID accountId, UUID workspaceId, Long eventId) {

        requireEvent(accountId, workspaceId, eventId);

        return categoryRepository.findAllByEventIdOrderByCreatedAtAscIdAsc(eventId)
                .stream()
                .map(category -> new CategoryResponse(category.getId(), category.getName()))
                .toList();
    }

    // 指定Event内のCategoryを単体取得する。
    @Transactional
    public CategoryResponse getCategoryById(UUID accountId, UUID workspaceId, Long eventId, Long categoryId) {

        requireEvent(accountId, workspaceId, eventId);
        CategoryEntity category = findCategory(categoryId, eventId);

        return new CategoryResponse(category.getId(), category.getName());
    }

    // 配下のNoteはDBのON DELETE CASCADEで削除される。
    @Transactional
    public void deleteCategory(UUID accountId, UUID workspaceId, Long eventId, Long categoryId) {

        requireEvent(accountId, workspaceId, eventId);
        CategoryEntity category = findCategory(categoryId, eventId);

        categoryRepository.delete(category);
    }

    // Workspaceへの所属と、EventがそのWorkspaceに属することを確認する。
    private EventEntity requireEvent(UUID accountId, UUID workspaceId, Long eventId) {

        workspaceRepository.findByIdAndAccountId(workspaceId, accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        return eventRepository.findByIdAndWorkspaceId(eventId, workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    // 別EventのCategoryを操作できないよう、Event IDも検索条件に含める。
    private CategoryEntity findCategory(Long categoryId, Long eventId) {

        return categoryRepository.findByIdAndEventId(categoryId, eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }
}
