CREATE TABLE exchange_rate
(
    id             UUID PRIMARY KEY,
    base_currency  CHAR(3)        NOT NULL,
    quote_currency CHAR(3)        NOT NULL,
    rate           NUMERIC(28, 10) NOT NULL CHECK (rate > 0),
    as_of          TIMESTAMPTZ    NOT NULL DEFAULT now()
);
CREATE INDEX idx_exchange_rate_pair_asof ON exchange_rate (base_currency, quote_currency, as_of DESC);

CREATE
RULE exchange_rate_no_update AS ON
UPDATE TO exchange_rate DO INSTEAD NOTHING;

CREATE
RULE exchange_rate_no_delete AS ON DELETE
TO exchange_rate DO INSTEAD NOTHING;

CREATE UNIQUE INDEX uq_fx_clearing_currency ON account (currency) WHERE type = 'FX_CLEARING';