CREATE TABLE posts
(
    id         BIGSERIAL PRIMARY KEY,
    author_id  BIGINT       NOT NULL,
    content    TEXT,
    visibility VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC',
    is_pinned  BOOLEAN      NOT NULL DEFAULT FALSE,
    metadata   JSONB,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_posts_author_id       ON posts (author_id);
CREATE INDEX idx_posts_created_at      ON posts (created_at DESC);
CREATE INDEX idx_posts_author_created  ON posts (author_id, created_at DESC);
CREATE INDEX idx_posts_visibility      ON posts (visibility) WHERE visibility = 'PUBLIC';

CREATE TABLE post_attachments
(
    id          BIGSERIAL PRIMARY KEY,
    post_id     BIGINT       NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    media_type  VARCHAR(20)  NOT NULL,
    url         VARCHAR(500) NOT NULL,
    preview_url VARCHAR(500),
    order_index INTEGER      NOT NULL DEFAULT 0,
    metadata    JSONB
);

CREATE INDEX idx_post_attachments_post_id ON post_attachments (post_id);

CREATE TABLE post_music_links
(
    id           BIGSERIAL PRIMARY KEY,
    post_id      BIGINT  NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    track_id     BIGINT  NOT NULL,
    listen_count INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_post_music_links_post_id ON post_music_links (post_id);

CREATE TABLE follows
(
    id          BIGSERIAL PRIMARY KEY,
    follower_id BIGINT      NOT NULL,
    followee_id BIGINT      NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_follows_pair UNIQUE (follower_id, followee_id)
);

CREATE INDEX idx_follows_follower       ON follows (follower_id);
CREATE INDEX idx_follows_followee       ON follows (followee_id);
CREATE INDEX idx_follows_follower_status ON follows (follower_id, status);

CREATE TABLE likes
(
    id         BIGSERIAL PRIMARY KEY,
    post_id    BIGINT      NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    user_id    BIGINT      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_likes_post_user UNIQUE (post_id, user_id)
);

CREATE INDEX idx_likes_post_id ON likes (post_id);
CREATE INDEX idx_likes_user_id ON likes (user_id);

CREATE TABLE comments
(
    id                BIGSERIAL PRIMARY KEY,
    post_id           BIGINT      NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    author_id         BIGINT      NOT NULL,
    content           TEXT        NOT NULL,
    parent_comment_id BIGINT               REFERENCES comments (id) ON DELETE CASCADE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comments_post_id   ON comments (post_id);
CREATE INDEX idx_comments_author_id ON comments (author_id);
CREATE INDEX idx_comments_parent    ON comments (parent_comment_id) WHERE parent_comment_id IS NOT NULL;

CREATE TABLE bookmarks
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    post_id    BIGINT      NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_bookmarks_user_post UNIQUE (user_id, post_id)
);

CREATE INDEX idx_bookmarks_user_id ON bookmarks (user_id);
CREATE INDEX idx_bookmarks_user_created ON bookmarks (user_id, created_at DESC);

CREATE TABLE post_stats
(
    post_id        BIGINT  PRIMARY KEY REFERENCES posts (id) ON DELETE CASCADE,
    likes_count    INTEGER NOT NULL DEFAULT 0,
    comments_count INTEGER NOT NULL DEFAULT 0,
    shares_count   INTEGER NOT NULL DEFAULT 0,
    views_count    INTEGER NOT NULL DEFAULT 0
);