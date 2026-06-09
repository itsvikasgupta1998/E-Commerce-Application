CREATE TABLE email_verification_token (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          token VARCHAR(255) NOT NULL,
                                          user_id BIGINT NOT NULL,
                                          expiry_date DATETIME NOT NULL,

                                          CONSTRAINT fk_email_token_user
                                              FOREIGN KEY (user_id) REFERENCES users(user_id)
                                                  ON DELETE CASCADE
);