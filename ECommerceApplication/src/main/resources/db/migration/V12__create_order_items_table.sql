CREATE TABLE order_items (
                             order_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             quantity INT,
                             discount DECIMAL(38,2),
                             ordered_product_price DECIMAL(38,2),

                             created_at TIMESTAMP,
                             updated_at TIMESTAMP,
                             deleted BOOLEAN DEFAULT FALSE,

                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,

                             CONSTRAINT fk_orderitem_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(order_id),

                             CONSTRAINT fk_orderitem_product
                                 FOREIGN KEY (product_id)
                                     REFERENCES products(product_id)
);