package com.kokorono_note.kokorono_note.repository;

import com.kokorono_note.kokorono_note.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    Optional<AccountEntity> findByGoogleSub(String googleSub);
}
