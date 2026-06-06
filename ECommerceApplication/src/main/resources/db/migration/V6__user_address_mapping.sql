CREATE TABLE user_addresses (
                              user_id BIGINT,
                              address_id BIGINT,

                              PRIMARY KEY (user_id, address_id),

                              FOREIGN KEY (user_id) REFERENCES users(user_id),
                              FOREIGN KEY (address_id) REFERENCES addresses(address_id)
);