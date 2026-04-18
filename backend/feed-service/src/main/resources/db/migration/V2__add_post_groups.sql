CREATE TABLE post_groups
(
    id         BIGSERIAL PRIMARY KEY,
    post_id    BIGINT      NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    group_id   BIGINT      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_post_group UNIQUE (post_id, group_id)
);

CREATE INDEX idx_post_groups_post_id ON post_groups (post_id);
CREATE INDEX idx_post_groups_group_id ON post_groups (group_id);