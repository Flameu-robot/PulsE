ALTER TABLE webauth DROP COLUMN IF EXISTS transports;

CREATE TABLE webauth_transports (
credential_id   BIGINT      NOT NULL,
transport       VARCHAR(20) NOT NULL,

CONSTRAINT fk_webauth_transports_credential
FOREIGN KEY (credential_id) REFERENCES webauth(id) ON DELETE CASCADE,

CONSTRAINT uq_webauth_transport
    UNIQUE (credential_id, transport)
);

CREATE INDEX idx_webauth_transports_credential_id ON webauth_transports(credential_id);