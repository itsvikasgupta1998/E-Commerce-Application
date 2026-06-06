CREATE TABLE categories (
                            category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            category_name VARCHAR(100) NOT NULL UNIQUE,

                            created_at TIMESTAMP,
                            updated_at TIMESTAMP,
                            deleted BOOLEAN DEFAULT FALSE
);