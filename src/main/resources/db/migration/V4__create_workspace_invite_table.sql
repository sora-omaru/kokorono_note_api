CREATE TABLE workspace_invite
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    workspace_id UUID NOT NULL,
    code CHAR(6) NOT NULL UNIQUE ,
    expires_at   TIMESTAMPTZ NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (workspace_id)
        REFERENCES workspace(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_workspace_invite_workspace_id
    ON workspace_invite (workspace_id);
