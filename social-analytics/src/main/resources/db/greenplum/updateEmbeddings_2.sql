INSERT INTO vector_store (message_id, msg_timestamp, metadata, embedding)
VALUES
    ( ?, ?, CAST(? AS JSONB),CAST(? AS VECTOR(1024)));