CREATE DATABASE short_url_db;
USE short_url_db;

CREATE TABLE short_url
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    short_code  VARCHAR(10) NOT NULL UNIQUE,
    long_url    TEXT        NOT NULL,
    created_at  DATETIME    NOT NULL,
    visit_count BIGINT DEFAULT 0,
    INDEX idx_short_code (short_code)
);