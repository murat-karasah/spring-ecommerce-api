-- ============================================================
-- V1: Initial schema — users, roles, categories, products,
--     carts, orders, and their join/child tables
-- ============================================================

-- ROLES
CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- USERS
CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- USER ↔ ROLE (Many-to-Many join table)
CREATE TABLE users_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- REFRESH TOKENS (for JWT refresh flow)
CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(512) NOT NULL UNIQUE,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE
);

-- CATEGORIES
CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- PRODUCTS
CREATE TABLE products (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    price          NUMERIC(19, 2) NOT NULL CHECK (price >= 0),
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    image_url      VARCHAR(512),
    category_id    BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

-- CARTS (1:1 with users)
CREATE TABLE carts (
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE
);

-- CART ITEMS
CREATE TABLE cart_items (
    id         BIGSERIAL PRIMARY KEY,
    cart_id    BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity   INT NOT NULL CHECK (quantity > 0),
    UNIQUE (cart_id, product_id)
);

-- ORDERS
CREATE TABLE orders (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id),
    total_amount     NUMERIC(19, 2) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    shipping_address TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ORDER ITEMS (price snapshot)
CREATE TABLE order_items (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity   INT NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19, 2) NOT NULL  -- snapshot of product.price at order time
);

-- Indexes for common query patterns
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_orders_user       ON orders(user_id);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_cart_items_cart   ON cart_items(cart_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
