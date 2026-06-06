CREATE TABLE payments
(
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    transaction_id VARCHAR(100) NOT NULL UNIQUE,

    payment_method VARCHAR(30) NOT NULL,

    payment_status VARCHAR(30) NOT NULL,

    amount DECIMAL(19,2) NOT NULL,

    gateway_name VARCHAR(100),

    gateway_response VARCHAR(255),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    order_id BIGINT NOT NULL UNIQUE,

    CONSTRAINT fk_payment_order
    FOREIGN KEY (order_id)
    REFERENCES orders(order_id)
);