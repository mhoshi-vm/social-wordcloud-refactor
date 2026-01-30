CREATE TABLE IF NOT EXISTS offset_store (
    collector TINYINT NOT NULL,
    pointer VARCHAR(255)
);