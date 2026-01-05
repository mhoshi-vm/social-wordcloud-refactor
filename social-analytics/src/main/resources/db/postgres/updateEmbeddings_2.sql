INSERT INTO vector_store (message_id, msg_timestamp, content, metadata, embedding)
VALUES
    ( ?, ?, ?, CAST(? AS JSONB),CAST(? AS REAL[]))