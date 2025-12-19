-- 1. Main Message Table
CREATE TABLE IF NOT EXISTS social_message (
    id                VARCHAR(64) NOT NULL,
    origin            VARCHAR(64),
    text              TEXT,
    lang              VARCHAR(2),
    name              TEXT,
    url               TEXT,
    create_date_time  TIMESTAMP,
    PRIMARY KEY (id)
);

-- 2. Sentiment Analysis (One-to-Many)
CREATE TABLE IF NOT EXISTS message_entity_sentiment (
    id                BIGINT AUTO_INCREMENT,
    message_id        VARCHAR(64),
    model_name        VARCHAR(255),
    sentiment_label   VARCHAR(255),
    confidence_score  FLOAT(24),
    PRIMARY KEY (id),
    CONSTRAINT fk_sentiment_message
        FOREIGN KEY (message_id) REFERENCES social_message (id)
        ON DELETE CASCADE
);

-- 3. Text Search Vectors (One-to-One)
CREATE TABLE IF NOT EXISTS message_entity_tsvector (
    message_id        VARCHAR(64) UNIQUE,
    word_vector       TEXT,
    PRIMARY KEY (message_id),
    CONSTRAINT fk_tsvector_message
        FOREIGN KEY (message_id) REFERENCES social_message (id)
        ON DELETE CASCADE
);

-- 4. Analytics Helpers
CREATE TABLE IF NOT EXISTS term_frequency_entity (
    rank              INTEGER NOT NULL,
    term              VARCHAR(255),
    count             INTEGER
);

-- Sequence for Primary Key generation
CREATE SEQUENCE stock_entity_seq
    START WITH 1
    INCREMENT BY 50;

-- Table for Stock Data
CREATE TABLE stock_entity (
    id        BIGINT NOT NULL,
    ticker    VARCHAR(255),
    price     FLOAT(24),
    volume    INTEGER,
    updated   TIMESTAMP(6) WITH TIME ZONE,
    PRIMARY KEY (id)
);