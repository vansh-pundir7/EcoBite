USE ecobite;

SET @old_safe_updates = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

UPDATE users SET username = CONCAT('user-', id) WHERE username IS NULL OR username = '';
UPDATE users SET password = '' WHERE password IS NULL;
UPDATE food SET food_name = 'Unknown food' WHERE food_name IS NULL OR food_name = '';
UPDATE food SET food_name = LEFT(food_name, 50) WHERE CHAR_LENGTH(food_name) > 50;
UPDATE food SET quantity = 1 WHERE quantity IS NULL OR quantity <= 0;
UPDATE food SET type = 'HUMAN' WHERE type IS NULL OR type = '';
UPDATE food SET prepared_time = NOW() WHERE prepared_time IS NULL;
UPDATE food SET expiry_time = DATE_ADD(NOW(), INTERVAL 1 HOUR) WHERE expiry_time IS NULL;
UPDATE food SET status = 'AVAILABLE' WHERE status IS NULL OR status = '';
DELETE r FROM requests r LEFT JOIN food f ON f.id = r.food_id WHERE f.id IS NULL;
UPDATE requests SET receiver_name = 'Unknown receiver' WHERE receiver_name IS NULL OR receiver_name = '';
UPDATE requests SET organization = 'Unknown organization' WHERE organization IS NULL OR organization = '';
UPDATE requests SET phone = '' WHERE phone IS NULL;
UPDATE requests SET status = 'PENDING' WHERE status IS NULL OR status = '';

ALTER TABLE users
    MODIFY username VARCHAR(50) NOT NULL,
    MODIFY password VARCHAR(255) NOT NULL;
ALTER TABLE food
    MODIFY food_name VARCHAR(50) NOT NULL,
    MODIFY quantity DOUBLE NOT NULL,
    MODIFY type VARCHAR(10) NOT NULL,
    MODIFY prepared_time DATETIME NOT NULL,
    MODIFY expiry_time DATETIME NOT NULL,
    MODIFY status VARCHAR(20) NOT NULL;
ALTER TABLE requests
    MODIFY food_id INT NOT NULL,
    MODIFY receiver_name VARCHAR(100) NOT NULL,
    MODIFY organization VARCHAR(100) NOT NULL,
    MODIFY phone VARCHAR(20) NOT NULL,
    MODIFY status VARCHAR(20) NOT NULL,
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE requests
    ADD CONSTRAINT fk_requests_food FOREIGN KEY (food_id) REFERENCES food(id);

SET SQL_SAFE_UPDATES = @old_safe_updates;
