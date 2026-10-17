-- ============================================
-- ECOBITE DATABASE (sirf 3 tables - simple!)
-- MySQL Workbench mein poora file run karo
-- ============================================

CREATE DATABASE IF NOT EXISTS ecobite;
USE ecobite;

-- 1. LOGIN USERS
CREATE TABLE IF NOT EXISTS users (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(50)
);

-- 2. FOOD
CREATE TABLE IF NOT EXISTS food (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    food_name     VARCHAR(100),
    quantity      DOUBLE,          -- KG mein
    type          VARCHAR(10),     -- HUMAN / ANIMAL
    prepared_time DATETIME,
    expiry_time   DATETIME,
    status        VARCHAR(20)      -- AVAILABLE / REQUESTED / ANIMAL_REVIEW / COMPLETED / DISCARDED
);

-- 3. REQUESTS
CREATE TABLE IF NOT EXISTS requests (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    food_id       INT,
    receiver_name VARCHAR(100),
    organization  VARCHAR(100),
    phone         VARCHAR(20),
    status        VARCHAR(20)      -- PENDING / COMPLETED / REJECTED
);

-- DEFAULT LOGIN:  admin / admin123
INSERT INTO users (username, password) VALUES ('admin', 'admin123');

-- SAMPLE DATA (demo ke liye - turant dikhne lagega)
INSERT INTO food (food_name, quantity, type, prepared_time, expiry_time, status) VALUES
('Rice',             10, 'HUMAN',  NOW(), DATE_ADD(NOW(), INTERVAL 4 HOUR),   'AVAILABLE'),
('Dal',               5, 'HUMAN',  NOW(), DATE_ADD(NOW(), INTERVAL 2 HOUR),   'AVAILABLE'),
('Bread',             2, 'HUMAN',  NOW(), DATE_ADD(NOW(), INTERVAL 1 MINUTE), 'AVAILABLE'),
('Vegetable Scraps',  8, 'ANIMAL', NOW(), DATE_ADD(NOW(), INTERVAL 24 HOUR),  'AVAILABLE');
-- NOTE: 'Bread' sirf 1 minute mein expire hoga ->
-- timer demo ke liye (status ANIMAL_REVIEW ho jayega)
