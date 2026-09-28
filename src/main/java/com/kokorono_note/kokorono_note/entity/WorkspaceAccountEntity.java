package com.kokorono_note.kokorono_note.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "workspace_account")
@Getter
@Setter
@NoArgsConstructor
public class WorkspaceAccountEntity {
    @EmbeddedId
    private WorkspaceAccountId id = new WorkspaceAccountId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("workspaceId")
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkspaceEntity workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("accountId")
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    @PrePersist
    public void prePersist() {
        if (this.joinedAt == null) {
            this.joinedAt = OffsetDateTime.now();
        }
    }

}
