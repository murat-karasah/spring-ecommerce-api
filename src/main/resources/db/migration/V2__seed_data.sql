-- ============================================================
-- V2: Seed data — roles + sample admin/user + categories
-- ============================================================

-- Roles
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- Admin user  (password: Admin1234!)
INSERT INTO users (email, password, first_name, last_name)
VALUES (
    'admin@example.com',
    '$2a$12$Hwy0KMUFoSjjxBosqjpZF.y0WT/LiPdqgZSoA2sNmApQf0yzBOnIC',
    'Admin',
    'User'
);

-- Regular user (password: User1234!)
INSERT INTO users (email, password, first_name, last_name)
VALUES (
    'user@example.com',
    '$2a$12$aTZL9O3NFtEtJVGWSq1uKeLs2Xew59J4ooWM2uTBKmJulOVsLIr2O',
    'Regular',
    'User'
);

-- Assign roles
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@example.com' AND r.name = 'ROLE_ADMIN';

INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@example.com' AND r.name = 'ROLE_USER';

INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'user@example.com' AND r.name = 'ROLE_USER';

-- Carts for seed users
INSERT INTO carts (user_id) SELECT id FROM users WHERE email = 'admin@example.com';
INSERT INTO carts (user_id) SELECT id FROM users WHERE email = 'user@example.com';

-- Sample categories
INSERT INTO categories (name, description) VALUES
    ('Electronics',    'Smartphones, laptops, tablets, and accessories'),
    ('Books',          'Fiction, non-fiction, and technical books'),
    ('Clothing',       'Men''s, women''s, and kids'' apparel'),
    ('Home & Kitchen', 'Furniture, appliances, and home decor'),
    ('Sports',         'Equipment, apparel, and accessories for sports');

-- Sample products
INSERT INTO products (name, description, price, stock_quantity, category_id)
VALUES
    ('MacBook Pro 14"', 'Apple M3 Pro chip, 18GB RAM, 512GB SSD', 1999.99, 15,
        (SELECT id FROM categories WHERE name = 'Electronics')),
    ('iPhone 15 Pro', 'Apple A17 Pro, 256GB, Titanium design', 1099.99, 30,
        (SELECT id FROM categories WHERE name = 'Electronics')),
    ('Clean Code', 'A Handbook of Agile Software Craftsmanship by Robert C. Martin', 35.00, 100,
        (SELECT id FROM categories WHERE name = 'Books')),
    ('Effective Java', '3rd Edition by Joshua Bloch', 45.00, 75,
        (SELECT id FROM categories WHERE name = 'Books')),
    ('Running Shoes Pro', 'Lightweight marathon running shoes, size 42', 120.00, 50,
        (SELECT id FROM categories WHERE name = 'Sports'));
