package com.kokorono_note.kokorono_note.repository;

import com.kokorono_note.kokorono_note.entity.RefreshTokenEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    void deleteAllByAccount_Id(UUID accountId);

    // 期限が基準時刻以前のトークンを、エンティティとして読み込まず一括削除する。
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity token WHERE token.expiresAt <= :now")
    int deleteExpiredTokens(@Param("now") OffsetDateTime now);
}
