
-- Viral Vault shared product catalog
-- New products are inserted automatically on backend startup.
-- Existing products and inventory are not overwritten.
-- Product IDs are generated automatically by SQLite.
-- To add a shared product, follow the format below.

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Labubu Exciting Macaron',
    'Collectibles',
    'Collectible plush figure from POP MART.',
    24.99,
    12,
    '/images/labubu.png'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Labubu Exciting Macaron'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'SMISKI Series 1',
    'Collectibles',
    'Glow-in-the-dark collectible figure.',
    12.99,
    8,
    '/images/smiski.png'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'SMISKI Series 1'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Sonny Angel Animal Series',
    'Collectibles',
    'Adorable miniature collectible figure.',
    14.99,
    0,
    '/images/sonny-angel.png'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Sonny Angel Animal Series'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Stanley Quencher H2.0',
    'Drinkware',
    'Popular 40 oz insulated tumbler with handle.',
    45.00,
    15,
    '/images/stanley.webp'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Stanley Quencher H2.0'
);