INSERT INTO message_entity_tsvector (message_id, word_vector)
SELECT id, 0.0 FROM social_message
WHERE id NOT IN (SELECT message_id FROM message_entity_tsvector)