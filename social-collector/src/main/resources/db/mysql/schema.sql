CREATE TABLE IF NOT EXISTS offset_store
(
    collector SMALLINT PRIMARY KEY NOT NULL,
    pointer TEXT
);