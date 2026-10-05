-- reset
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

-- users
CREATE TABLE users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(100),  -- Added for prototype
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    birthday DATE,           -- Added for prototype
    address VARCHAR(255),    -- Added for prototype
    city VARCHAR(100),       -- Added for prototype
    state VARCHAR(50),       -- Added for prototype
    zip_code VARCHAR(20),    -- Added for prototype
    is_admin BOOLEAN DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- products table
CREATE TABLE products (
    product_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0, 
    image_url VARCHAR(255),
    is_on_sale BOOLEAN DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- orders table
CREATE TABLE orders (
    order_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'Pending',
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- order items
CREATE TABLE order_items (
    order_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    price_at_purchase DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- items in cart
CREATE TABLE cart_items (
    cart_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- fill in data
INSERT INTO users (username, email, password_hash, is_admin) 
VALUES ('admin_user', 'admin@viralvault.com', 'hashedpassword123', 1),
       ('test_shopper', 'shopper@email.com', 'hashedpassword456', 0);

INSERT INTO products (name, category, description, price, quantity, image_url, is_on_sale)
VALUES ('Labubu', 'Blind boxes', 'Trending blind box figures', 15.99, 50, '/images/labubu.jpg', 0),
       ('Stanley 40oz', 'Lifestyle', ' Insulated Tumbler', 45.00, 120, '/images/stanley.jpg', 1),
       ('Needoh', 'Fidgets/Squishies', 'Squishy Stress ball', 13.99, 50, '/images/needoh.jpg', 1);