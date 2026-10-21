package com.kokorono_note.kokorono_note.repository;

import com.kokorono_note.kokorono_note.entity.NoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, Long> {
    List<NoteEntity> findAllByCategoryIdOrderByCreatedAtAscIdAsc(Long categoryId);

    Optional<NoteEntity> findByIdAndCategoryId(Long noteId, Long categoryId);

}
