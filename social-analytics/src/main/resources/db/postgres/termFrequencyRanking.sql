INSERT INTO term_frequency (rank, term, count)
SELECT ROW_NUMBER() OVER (ORDER BY nentry desc )
AS rank, word as term, nentry as count
FROM ts_stat('SELECT word_vector FROM social_message') ORDER BY nentry DESC
