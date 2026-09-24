CREATE TABLE account
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    google_sub   VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(100),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);