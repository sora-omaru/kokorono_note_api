CREATE TABLE workspace_account
(
    workspace_id UUID        NOT NULL,
    account_id   UUID        NOT NULL,
    joined_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (workspace_id, account_id),

    FOREIGN KEY (workspace_id)
        REFERENCES workspace (id)
        ON DELETE CASCADE,

    FOREIGN KEY (account_id)
        REFERENCES account (id)
        ON DELETE CASCADE
);

