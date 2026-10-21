package com.kokorono_note.kokorono_note.controller;

import com.kokorono_note.kokorono_note.dto.request.CategoryCreateRequest;
import com.kokorono_note.kokorono_note.dto.response.CategoryResponse;
import com.kokorono_note.kokorono_note.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/events/{eventId}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 一覧取得
    @GetMapping
    public List<CategoryResponse> getCategories(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("eventId") Long eventId
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return categoryService.getCategories(accountId, workspaceId, eventId);
    }

    // 単体取得
    @GetMapping("/{categoryId}")
    public CategoryResponse getCategoryById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("eventId") Long eventId,
            @PathVariable("categoryId") Long categoryId
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return categoryService.getCategoryById(accountId, workspaceId, eventId, categoryId);
    }

    // 作成
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return categoryService.createCategory(accountId, workspaceId, eventId, request);
    }

    // 削除
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("eventId") Long eventId,
            @PathVariable("categoryId") Long categoryId
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        categoryService.deleteCategory(accountId, workspaceId, eventId, categoryId);
    }
}
