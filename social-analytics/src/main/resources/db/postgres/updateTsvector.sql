INSERT INTO message_entity_tsvector (message_id, word_vector)
SELECT id, to_tsvector('english', text) FROM social_message
WHERE id NOT IN (SELECT message_id FROM message_entity_tsvector);
