-- ===== Users =====
CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    phone           VARCHAR(20),
    password_hash   VARCHAR(255),
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    role            VARCHAR(20)     NOT NULL DEFAULT 'USER',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    last_login_at   TIMESTAMPTZ,
    avatar_url      VARCHAR(500),
    bio             VARCHAR(500),
    metadata        JSONB           DEFAULT '{}'::jsonb,

    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT uq_users_phone    UNIQUE (phone)
);

-- ===== WebAuthn Credentials =====
CREATE TABLE webauth (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    credential_id   BYTEA           NOT NULL,
    public_key      BYTEA           NOT NULL,
    sign_count      BIGINT          NOT NULL DEFAULT 0,
    transports      VARCHAR(50),
    aaguid          UUID,
    device_name     VARCHAR(100),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT fk_webauthn_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_webauthn_credential_id UNIQUE (credential_id)
);

-- ===== OAuth2 Accounts =====
CREATE TABLE oauth2 (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    provider        VARCHAR(20)     NOT NULL,
    provider_id     VARCHAR(255)    NOT NULL,
    linked_at       TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT fk_oauth2_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_oauth2_provider_id UNIQUE (provider, provider_id)
);

-- ===== Refresh Tokens =====
CREATE TABLE tokens (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    token_hash      VARCHAR(255)    NOT NULL,
    is_revoked      BOOLEAN         NOT NULL DEFAULT false,
    expires_at      TIMESTAMPTZ     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    user_agent      VARCHAR(255),
    ip_address      INET,

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash)
);

-- ===== Verification Tokens =====
CREATE TABLE verification (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    token           VARCHAR(255)    NOT NULL,
    type            VARCHAR(20)     NOT NULL,
    expires_at      TIMESTAMPTZ     NOT NULL,
    used            BOOLEAN         NOT NULL DEFAULT false,

    CONSTRAINT fk_verification_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_verification_token UNIQUE (token)
);

-- ===== Индексы =====
CREATE INDEX idx_users_username             ON users(username);
CREATE INDEX idx_users_email                ON users(email);
CREATE INDEX idx_webauth_user_id            ON webauth(user_id);
CREATE INDEX idx_webauth_credential_id      ON webauth(credential_id);
CREATE INDEX idx_oauth2_user_id             ON oauth2(user_id);
CREATE INDEX idx_tokens_user_id             ON tokens(user_id);
CREATE INDEX idx_tokens_token_hash          ON tokens(token_hash);
CREATE INDEX idx_verification_user_id       ON verification(user_id);
CREATE INDEX idx_verification_token         ON verification(token);