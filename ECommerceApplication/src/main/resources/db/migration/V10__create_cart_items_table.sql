CREATE TABLE cart_items (
                            cart_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            quantity INT,
                            discount DECIMAL(38,2),
                            product_price DECIMAL(38,2),

                            created_at TIMESTAMP,
                            updated_at TIMESTAMP,
                            deleted BOOLEAN DEFAULT FALSE,

                            cart_id BIGINT NOT NULL,
                            product_id BIGINT NOT NULL,

                            CONSTRAINT fk_cartitem_cart
                                FOREIGN KEY (cart_id)
                                    REFERENCES carts(cart_id),

                            CONSTRAINT fk_cartitem_product
                                FOREIGN KEY (product_id)
                                    REFERENCES products(product_id)
);