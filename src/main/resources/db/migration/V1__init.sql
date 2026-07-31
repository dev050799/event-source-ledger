CREATE TABLE account
(
    id           UUID PRIMARY KEY,
    name         TEXT        NOT NULL,
    type         TEXT        NOT NULL,
    normal_side  CHAR(2)     NOT NULL CHECK (normal_side IN ('DR', 'CR')),
    currency     CHAR(3)     NOT NULL,
    credit_limit BIGINT      NOT NULL DEFAULT 0 CHECK (credit_limit >= 0),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ledger_transaction
(
    id                     UUID PRIMARY KEY,
    sequence               BIGSERIAL UNIQUE NOT NULL,
    type                   TEXT             NOT NULL,
    effective_at           TIMESTAMPTZ      NOT NULL,
    recorded_at            TIMESTAMPTZ      NOT NULL DEFAULT now(),
    idempotency_key        TEXT UNIQUE,
    reverse_transaction_id UUID REFERENCES ledger_transaction (id),
    metadata               JSONB            NOT NULL DEFAULT '{}'::jsonb
);
CREATE INDEX idx_txn_effective_at ON ledger_transaction (effective_at);

CREATE TABLE entry
(
    id             UUID PRIMARY KEY,
    transaction_id UUID    NOT NULL REFERENCES ledger_transaction (id),
    account_id     UUID    NOT NULL REFERENCES account (id),
    direction      CHAR(2) NOT NULL CHECK ( direction IN ('DR', 'CR')),
    amount         BIGINT  NOT NULL CHECK ( amount > 0 ),
    currency       CHAR(3) NOT NULL
);
CREATE INDEX idx_entry_account ON entry (account_id);
CREATE INDEX idx_entry_transaction ON entry (transaction_id);

CREATE TABLE account_balance
(
    account_id    UUID PRIMARY KEY REFERENCES account (id),
    balance       BIGINT NOT NULL DEFAULT 0,
    credit_limit  BIGINT NOT NULL DEFAULT 0,
    last_sequence BIGINT NOT NULL DEFAULT 0,
    version       BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT no_overdraft CHECK ( balance >= -credit_limit )
);

CREATE TABLE account_snapshot
(
    account_id     UUID        NOT NULL REFERENCES account (id),
    as_of_sequence BIGINT      NOT NULL,
    as_of_time     TIMESTAMPTZ NOT NULL,
    balance        BIGINT      NOT NULL,
    entry_count    BIGINT      NOT NULL,
    PRIMARY KEY (account_id, as_of_sequence)
);

CREATE
RULE journal_no_update AS ON
UPDATE TO ledger_transaction DO INSTEAD NOTHING;

CREATE
RULE journal_no_delete AS ON DELETE
TO ledger_transaction DO INSTEAD NOTHING;

CREATE
RULE entry_no_update AS ON
UPDATE TO entry DO INSTEAD NOTHING;

CREATE
RULE entry_no_delete AS ON DELETE
TO entry DO INSTEAD NOTHING;
