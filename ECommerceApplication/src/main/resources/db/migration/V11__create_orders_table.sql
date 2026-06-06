CREATE TABLE orders (
                        order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        email VARCHAR(100),
                        order_date DATE,
                        total_amount DECIMAL(38,2),
                        order_status VARCHAR(50) NOT NULL,

                        created_at TIMESTAMP,
                        updated_at TIMESTAMP,
                        deleted BOOLEAN DEFAULT FALSE,

                        user_id BIGINT NOT NULL,

                        CONSTRAINT fk_order_user
                        FOREIGN KEY (user_id)
                        REFERENCES users(user_id)
);