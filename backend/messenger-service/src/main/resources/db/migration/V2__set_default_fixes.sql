ALTER TABLE channel_read_states
    ADD created_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE channel_read_states
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE attachments
    ADD is_deleted BOOLEAN DEFAULT FALSE;

ALTER TABLE attachments
    ALTER COLUMN is_deleted SET NOT NULL;

ALTER TABLE text_channels
    ALTER COLUMN position SET DEFAULT 0;