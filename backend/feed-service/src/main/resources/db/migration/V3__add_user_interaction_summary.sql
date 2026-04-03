CREATE TABLE user_interaction_summary (
                                          id             BIGSERIAL PRIMARY KEY,
                                          user_id        BIGINT         NOT NULL,
                                          author_id      BIGINT         NOT NULL,
                                          like_count     INT            NOT NULL DEFAULT 0,
                                          bookmark_count INT            NOT NULL DEFAULT 0,
                                          view_count     INT            NOT NULL DEFAULT 0,
                                          score          DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                                          updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

                                          CONSTRAINT uq_interaction_user_author UNIQUE (user_id, author_id)
);

CREATE INDEX idx_interaction_user_score ON user_interaction_summary (user_id, score DESC);
CREATE INDEX idx_interaction_updated    ON user_interaction_summary (updated_at);

CREATE INDEX IF NOT EXISTS idx_post_stats_score
    ON post_stats (likes_count DESC, views_count DESC);