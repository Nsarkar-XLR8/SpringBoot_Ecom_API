-- Runs after baseline in existing environments where V1 was skipped.

ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS products ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS categories ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS carts ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS cart_items ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS orders ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS order_items ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS checkout_sessions ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS checkout_session_items ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE IF EXISTS checkout_sessions
    ADD COLUMN IF NOT EXISTS stripe_payment_intent VARCHAR(255);
ALTER TABLE IF EXISTS checkout_sessions
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(255);

UPDATE checkout_sessions
SET idempotency_key = CONCAT('legacy-', stripe_session_id)
WHERE idempotency_key IS NULL;

ALTER TABLE IF EXISTS checkout_sessions
    ALTER COLUMN idempotency_key SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_checkout_sessions_idempotency_key
    ON checkout_sessions (idempotency_key);

CREATE TABLE IF NOT EXISTS webhook_events (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    event_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(255) NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE IF EXISTS orders
    ALTER COLUMN payment_validated SET DEFAULT false;
UPDATE orders SET payment_validated = false
WHERE payment_validated IS NULL;
ALTER TABLE IF EXISTS orders
    ALTER COLUMN payment_validated DROP NOT NULL;
