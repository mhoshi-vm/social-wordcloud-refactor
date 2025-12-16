CREATE TABLE IF NOT EXISTS social_message
(
    id TEXT PRIMARY KEY NOT NULL,
    origin TEXT,
    text TEXT,
    lang TEXT,
    name TEXT,
    url TEXT,
    create_date_time TIMESTAMP,
    sentiment TEXT NULL ,
    sentiment_score FLOAT NULL,
    action TEXT,
    word_vector TSVECTOR
);

CREATE TABLE IF NOT EXISTS term_frequency
(
    rank INT PRIMARY KEY NOT NULL,
    term TEXT,
    count INT
);


