-- 1. Main Message Table
CREATE TABLE IF NOT EXISTS social_message (
    id                VARCHAR(64) NOT NULL,
    origin            VARCHAR(64),
    text              TEXT,
    lang              VARCHAR(2),
    name              TEXT,
    url               TEXT,
    create_date_time  TIMESTAMP,
    action            TINYINT CHECK (action BETWEEN 0 AND 2),
    PRIMARY KEY (id)
);

-- 2. Sentiment Analysis (One-to-Many)
CREATE TABLE IF NOT EXISTS message_entity_sentiment (
    id                BIGINT NOT NULL,
    message_id        VARCHAR(255),
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
    id                BIGINT NOT NULL,
    message_id        VARCHAR(255) UNIQUE,
    word_vector       TEXT,
    PRIMARY KEY (id),
    CONSTRAINT fk_tsvector_message 
        FOREIGN KEY (message_id) REFERENCES social_message (id) 
        ON DELETE CASCADE
);

-- 4. Analytics Helpers
CREATE TABLE IF NOT EXISTS term_frequency_entity (
    rank              INTEGER NOT NULL,
    term              VARCHAR(255),
    count             INTEGER,
    PRIMARY KEY (rank)
);
