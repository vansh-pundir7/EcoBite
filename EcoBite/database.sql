CREATE DATABASE IF NOT EXISTS ecobite;
USE ecobite;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS food (
    id INT AUTO_INCREMENT PRIMARY KEY,
    food_name VARCHAR(50) NOT NULL,
    quantity DOUBLE NOT NULL,
    type VARCHAR(10) NOT NULL,
    prepared_time DATETIME NOT NULL,
    expiry_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    food_id INT NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    organization VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_requests_food FOREIGN KEY (food_id) REFERENCES food(id)
);

INSERT INTO food (food_name, quantity, type, prepared_time, expiry_time, status) VALUES
('Rice', 10, 'HUMAN', NOW(), DATE_ADD(NOW(), INTERVAL 4 HOUR), 'AVAILABLE'),
('Dal', 5, 'HUMAN', NOW(), DATE_ADD(NOW(), INTERVAL 2 HOUR), 'AVAILABLE'),
('Bread', 2, 'HUMAN', NOW(), DATE_ADD(NOW(), INTERVAL 1 MINUTE), 'AVAILABLE'),
('Vegetable Scraps', 8, 'ANIMAL', NOW(), DATE_ADD(NOW(), INTERVAL 24 HOUR), 'AVAILABLE');
