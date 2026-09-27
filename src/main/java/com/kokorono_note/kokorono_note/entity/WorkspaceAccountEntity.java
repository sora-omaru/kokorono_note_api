package com.kokorono_note.kokorono_note.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceAccountEntity {
    @EmbeddedId
    private WorkspaceAccountEntity id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("workspaceId")
    @JoinColumn(name = "workspace_id",nullable = false)
    private WorkspaceEntity workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("accountId")
    @JoinColumn(name = "account_id",nullable = false)
    private AccountEntity account;

    @Column(name = "joined_at",nullable = false)
    private OffsetDateTime joinedAt;
}
