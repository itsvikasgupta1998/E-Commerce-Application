CREATE TABLE addresses (
                           address_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           street VARCHAR(100),
                           building_name VARCHAR(100),
                           city VARCHAR(50),
                           state VARCHAR(50),
                           country VARCHAR(50),
                           pincode VARCHAR(10),

                           created_at TIMESTAMP,
                           updated_at TIMESTAMP,
                           deleted BOOLEAN DEFAULT FALSE,

                           CONSTRAINT uk_address
                               UNIQUE(
                                      street,
                                      building_name,
                                      city,
                                      state,
                                      country,
                                      pincode
                                   )
);