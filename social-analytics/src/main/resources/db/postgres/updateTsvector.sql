UPDATE social_message
SET word_vector=to_tsvector('english', text)
WHERE word_vector IS NULL