INSERT INTO vector_store (message_id, content, metadata, embedding)
VALUES
    ( ?, ?, CAST(? AS JSONB),CAST(? AS REAL[]))