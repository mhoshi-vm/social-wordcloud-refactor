SELECT * FROM social_message
WHERE id NOT IN (SELECT message_id FROM vector_store)