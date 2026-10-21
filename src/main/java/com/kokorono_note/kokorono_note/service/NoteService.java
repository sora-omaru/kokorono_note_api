package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.dto.request.NoteCreateRequest;
import com.kokorono_note.kokorono_note.dto.request.NoteUpdateRequest;
import com.kokorono_note.kokorono_note.dto.response.NoteResponse;
import com.kokorono_note.kokorono_note.entity.CategoryEntity;
import com.kokorono_note.kokorono_note.entity.NoteEntity;
import com.kokorono_note.kokorono_note.repository.CategoryRepository;
import com.kokorono_note.kokorono_note.repository.EventRepository;
import com.kokorono_note.kokorono_note.repository.NoteRepository;
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
public class NoteService {
    private final WorkspaceRepository workspaceRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final NoteRepository noteRepository;

    // 指定CategoryにNoteを作成する。
    @Transactional
    public NoteResponse createNote(UUID accountId, UUID workspaceId, Long eventId, Long categoryId,
                                   NoteCreateRequest request) {

        CategoryEntity category = requireCategory(accountId, workspaceId, eventId, categoryId);

        NoteEntity note = new NoteEntity();
        note.setCategory(category);
        note.setContent(request.content().trim());

        NoteEntity saved = noteRepository.save(note);

        return new NoteResponse(saved.getId(), saved.getContent());
    }

    // 登録日時順で一覧を取得する。同じ日時の場合はID順とする。
    @Transactional
    public List<NoteResponse> getNotes(UUID accountId, UUID workspaceId, Long eventId, Long categoryId) {

        requireCategory(accountId, workspaceId, eventId, categoryId);

        return noteRepository.findAllByCategoryIdOrderByCreatedAtAscIdAsc(categoryId)
                .stream()
                .map(note -> new NoteResponse(note.getId(), note.getContent()))
                .toList();
    }

    // 指定Category内のNoteを単体取得する。
    @Transactional
    public NoteResponse getNoteById(UUID accountId, UUID workspaceId, Long eventId, Long categoryId, Long noteId) {

        requireCategory(accountId, workspaceId, eventId, categoryId);
        NoteEntity note = findNote(noteId, categoryId);

        return new NoteResponse(note.getId(), note.getContent());
    }

    // 本文のみを更新する。トランザクション終了時にJPAが変更を保存する。
    @Transactional
    public NoteResponse updateNote(UUID accountId, UUID workspaceId, Long eventId, Long categoryId, Long noteId,
                                   NoteUpdateRequest request) {

        requireCategory(accountId, workspaceId, eventId, categoryId);
        NoteEntity note = findNote(noteId, categoryId);
        note.setContent(request.content().trim());

        return new NoteResponse(note.getId(), note.getContent());
    }

    // 指定Category内のNoteを削除する。
    @Transactional
    public void deleteNote(UUID accountId, UUID workspaceId, Long eventId, Long categoryId, Long noteId) {

        requireCategory(accountId, workspaceId, eventId, categoryId);
        NoteEntity note = findNote(noteId, categoryId);

        noteRepository.delete(note);
    }

    // Workspaceへの所属と、Event・Categoryの親子関係を順番に確認する。
    private CategoryEntity requireCategory(UUID accountId, UUID workspaceId, Long eventId, Long categoryId) {

        workspaceRepository.findByIdAndAccountId(workspaceId, accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        eventRepository.findByIdAndWorkspaceId(eventId, workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        return categoryRepository.findByIdAndEventId(categoryId, eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    // 別CategoryのNoteを操作できないよう、Category IDも検索条件に含める。
    private NoteEntity findNote(Long noteId, Long categoryId) {

        return noteRepository.findByIdAndCategoryId(noteId, categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
    }
}
