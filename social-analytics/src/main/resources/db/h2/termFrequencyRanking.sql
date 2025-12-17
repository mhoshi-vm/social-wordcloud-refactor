INSERT INTO term_frequency (rank, term, count)
SELECT
    ROW_NUMBER() OVER (ORDER BY create_date_time) AS rank,
    LEFT(RANDOM_UUID(), 8) as term,
    CAST(RAND() * 1000 AS INT) as count
FROM social_message

