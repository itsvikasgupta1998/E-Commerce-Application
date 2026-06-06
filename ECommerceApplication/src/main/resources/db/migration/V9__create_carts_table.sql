CREATE TABLE carts (
                       cart_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       total_price DECIMAL(38,2),
                       created_at TIMESTAMP,
                       updated_at TIMESTAMP,
                       deleted BOOLEAN DEFAULT FALSE,
                       user_id BIGINT UNIQUE,

                       CONSTRAINT fk_cart_user
                       FOREIGN KEY (user_id)
                       REFERENCES users(user_id)
);