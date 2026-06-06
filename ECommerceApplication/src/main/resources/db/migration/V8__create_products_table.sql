CREATE TABLE products (
                          product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          product_name VARCHAR(255) NOT NULL,
                          image VARCHAR(255),
                          description TEXT,
                          quantity INT NOT NULL DEFAULT 0,
                          price DECIMAL(19,2) NOT NULL,
                          discount DECIMAL(19,2) NOT NULL DEFAULT 0,
                          special_price DECIMAL(19,2) NOT NULL,
                          category_id BIGINT NOT NULL,
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP,
                          deleted BOOLEAN DEFAULT FALSE,
                          sku VARCHAR(50) NOT NULL UNIQUE,

                          CONSTRAINT fk_product_category
                          FOREIGN KEY (category_id)
                          REFERENCES categories(category_id)
);