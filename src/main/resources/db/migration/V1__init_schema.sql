-- ============================================================
-- V1__init_schema.sql
-- Complete schema matching all JPA entities
-- ============================================================

-- 1. users
CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    version             BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    name                VARCHAR(255) NOT NULL,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password            VARCHAR(255) NOT NULL,
    role                VARCHAR(50) DEFAULT 'CUSTOMER',
    stripe_customer_id  VARCHAR(255)
);

-- 2. categories
CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    version     BIGINT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

-- 3. products
CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    version     BIGINT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    price       DECIMAL(19, 2) NOT NULL,
    stock       INTEGER NOT NULL,
    image_url   VARCHAR(255),
    status      VARCHAR(50) DEFAULT 'ACTIVE',
    category_id BIGINT,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

-- 4. carts
CREATE TABLE carts (
    id         BIGSERIAL PRIMARY KEY,
    version    BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    user_id    BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 5. cart_items
CREATE TABLE cart_items (
    id         BIGSERIAL PRIMARY KEY,
    version    BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    quantity   INTEGER NOT NULL,
    cart_id    BIGINT,
    product_id BIGINT,
    CONSTRAINT fk_cart_items_cart    FOREIGN KEY (cart_id)    REFERENCES carts (id),
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- 6. orders
CREATE TABLE orders (
    id          BIGSERIAL PRIMARY KEY,
    version     BIGINT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    status      VARCHAR(50) DEFAULT 'PENDING',
    total_price DECIMAL(19, 2) NOT NULL,
    user_id     BIGINT,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 7. order_items
CREATE TABLE order_items (
    id         BIGSERIAL PRIMARY KEY,
    version    BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    quantity   INTEGER NOT NULL,
    price      DECIMAL(19, 2) NOT NULL,
    order_id   BIGINT,
    product_id BIGINT,
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- 8. addresses
CREATE TABLE addresses (
    id         BIGSERIAL PRIMARY KEY,
    version    BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    street     VARCHAR(255) NOT NULL,
    city       VARCHAR(255) NOT NULL,
    state      VARCHAR(255) NOT NULL,
    country    VARCHAR(255) NOT NULL,
    zip_code   VARCHAR(255) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    user_id    BIGINT NOT NULL,
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 9. wishlists
CREATE TABLE wishlists (
    id         BIGSERIAL PRIMARY KEY,
    version    BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    user_id    BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_wishlists_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 10. wishlist_products (join table)
CREATE TABLE wishlist_products (
    wishlist_id BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    PRIMARY KEY (wishlist_id, product_id),
    CONSTRAINT fk_wp_wishlist FOREIGN KEY (wishlist_id) REFERENCES wishlists (id),
    CONSTRAINT fk_wp_product  FOREIGN KEY (product_id)  REFERENCES products (id)
);

-- 11. reviews
CREATE TABLE reviews (
    id         BIGSERIAL PRIMARY KEY,
    version    BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    rating     INTEGER NOT NULL,
    comment    VARCHAR(1000) NOT NULL,
    user_id    BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    CONSTRAINT fk_reviews_user    FOREIGN KEY (user_id)    REFERENCES users (id),
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- 12. coupons
CREATE TABLE coupons (
    id               BIGSERIAL PRIMARY KEY,
    version          BIGINT NOT NULL DEFAULT 0,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP,
    code             VARCHAR(255) NOT NULL UNIQUE,
    discount_type    VARCHAR(50) NOT NULL,
    discount_value   DECIMAL(19, 2) NOT NULL,
    min_order_amount DECIMAL(19, 2) NOT NULL,
    expiry_date      TIMESTAMP NOT NULL,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    usage_limit      INTEGER NOT NULL DEFAULT 100,
    usage_count      INTEGER NOT NULL DEFAULT 0
);

-- 13. checkout_sessions
CREATE TABLE checkout_sessions (
    id                     BIGSERIAL PRIMARY KEY,
    version                BIGINT NOT NULL DEFAULT 0,
    created_at             TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP,
    user_id                BIGINT NOT NULL,
    stripe_session_id      VARCHAR(255) NOT NULL UNIQUE,
    stripe_payment_intent  VARCHAR(255),
    idempotency_key        VARCHAR(255) NOT NULL UNIQUE,
    checkout_url           VARCHAR(1200) NOT NULL,
    payment_status         VARCHAR(50) DEFAULT 'PENDING',
    processed              BOOLEAN NOT NULL DEFAULT FALSE,
    total_price            DECIMAL(19, 2) NOT NULL,
    currency               VARCHAR(8) DEFAULT 'usd',
    expires_at             TIMESTAMP,
    order_id               BIGINT,
    CONSTRAINT fk_cs_user  FOREIGN KEY (user_id)  REFERENCES users (id),
    CONSTRAINT fk_cs_order FOREIGN KEY (order_id)  REFERENCES orders (id)
);

-- 14. checkout_session_items
CREATE TABLE checkout_session_items (
    id                  BIGSERIAL PRIMARY KEY,
    version             BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    checkout_session_id BIGINT NOT NULL,
    product_id          BIGINT NOT NULL,
    quantity            INTEGER NOT NULL,
    unit_price          DECIMAL(19, 2) NOT NULL,
    CONSTRAINT fk_csi_session FOREIGN KEY (checkout_session_id) REFERENCES checkout_sessions (id),
    CONSTRAINT fk_csi_product FOREIGN KEY (product_id)          REFERENCES products (id)
);

-- 15. webhook_events
CREATE TABLE webhook_events (
    id         BIGSERIAL PRIMARY KEY,
    version    BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    event_id   VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(255) NOT NULL,
    processed  BOOLEAN NOT NULL DEFAULT FALSE
);
