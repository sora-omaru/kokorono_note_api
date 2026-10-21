package com.kokorono_note.kokorono_note.repository;

import com.kokorono_note.kokorono_note.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findAllByEventIdOrderByCreatedAtAscIdAsc(Long eventId);

    Optional<CategoryEntity> findByIdAndEventId(Long categoryId, Long eventId);

}
