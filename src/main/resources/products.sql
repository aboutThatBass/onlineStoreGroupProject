
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

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Blue Needoh',
    'Squishy',
    'Trendy squishy cube to fidget and destress.',
    29.99,
    0,
    '/images/needoh.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Blue Needoh'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Glitter Mystery Bao Bun',
    'Squishy',
    'Glittery squishy bao bun.',
    10.00,
    20,
    '/images/dumpling.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Glitter Mystery Bao Bun'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Needoh Advent Calendar',
    'Squishy',
    'Festive Needohs for the holiday season. 24 days of surprise.',
    14.99,
    5,
    '/images/needohcalendar.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Needoh Advent Calendar'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Butter Squishy',
    'Squishy',
    'Squishy that looks like a stick of butter.',
    9.99,
    60,
    '/images/butter.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Butter Squishy'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Crunchy Soap',
    'Squishy',
    'Soap squishy that crunches when you squeeze it.',
    9.00,
    0,
    '/images/soap.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Crunchy Soap'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'SkullPanda x My Little Ponny',
    'Collectibles',
    'Super cute blind box plushies collabed with My Little Ponny.',
    70.00,
    7,
    '/images/skullpandamlp.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'SkullPanda x My Little Ponny'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Heymi Witches Game',
    'Collectibles',
    'Super cute blind box plushies.',
    45.00,
    15,
    '/images/heymi.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Heymi Witches Game'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Monchichi',
    'Collectibles',
    'rare mystery monkey plushie blind box.',
    180.00,
    1,
    '/images/monchichi.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Monchichi'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Snoopy Starbucks',
    'Drinkware',
    'Limited edition Snoopy x Starbucks glass cup.',
    400.00,
    2,
    '/images/esnoopy.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Snoopy Starbucks'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Peachybbies Pastel Cloud Slime',
    'Slime',
    'Cute soft pastel cloud slime.',
    16.99,
    20,
    '/images/cloudslime.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Peachybbies Pastel Cloud Slime'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Peachybbies Boba Slime Kit',
    'Slime',
    'Boba + Slime = yummy fun! DO NOT EAT.',
    19.99,
    33,
    '/images/boba.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Peachybbies Boba Slime Kit'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Cheese Squishy',
    'Squishy',
    'Trendy squishy cheese.',
    18.99,
    6,
    '/images/cheese.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Cheese Squishy'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Owala Special Edition Cowgirl',
    'Drinkware',
    'Special edition Owala with a cowgirl print.',
    40.00,
    4,
    '/images/owalacow.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Owala Special Edition Cowgirl'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Starbucks Bearista Holiday Cup',
    'Drinkware',
    'Mystery blind box of the famous bearista cup.',
    50.99,
    12,
    '/images/starbucks.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Starbucks Bearista Holiday Cup'
);

INSERT INTO products
(name, category, description, price, quantity, image_url)
SELECT
    'Bearista Cup',
    'Drinkware',
    'Starbucks cute bear glass cup.',
    40.99,
    8,
    '/images/bear.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM products
    WHERE name = 'Bearista Cup'
);