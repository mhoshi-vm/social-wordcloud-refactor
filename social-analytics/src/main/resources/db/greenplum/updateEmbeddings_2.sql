INSERT INTO vector_store (message_id, msg_timestamp, content, metadata, embedding)
VALUES
    ( ?, ?, ?, CAST(? AS JSONB),CAST(? AS VECTOR(1024)))
ON CONFLICT (message_id, msg_timestamp) DO
UPDATE
SET content = EXCLUDED.content, metadata = EXCLUDED.metadata, embedding = EXCLUDED.embedding;