package com.kokorono_note.kokorono_note.repository;

import com.kokorono_note.kokorono_note.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {

}
