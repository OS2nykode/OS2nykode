CREATE TABLE audit_log (
    id                      BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    timestamp               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_cpr                VARCHAR(128) NOT NULL,
    user_name               VARCHAR(128) NOT NULL,
    changed_account         VARCHAR(128) NOT NULL
);