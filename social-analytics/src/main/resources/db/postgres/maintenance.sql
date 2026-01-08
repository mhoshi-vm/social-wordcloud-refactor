VACUUM ANALYZE;

-- Cleanup Orphans
-- 1. Clean Sentiment
DELETE FROM message_entity_sentiment
WHERE NOT EXISTS (
    SELECT 1 FROM social_message p
    WHERE p.id = message_entity_sentiment.message_id
    AND p.create_date_time = message_entity_sentiment.msg_timestamp
);

-- 2. Clean TSVector
DELETE FROM message_entity_tsvector
WHERE NOT EXISTS (
    SELECT 1 FROM social_message p
    WHERE p.id = message_entity_tsvector.message_id
    AND p.create_date_time = message_entity_tsvector.msg_timestamp
);

-- 3. Clean Vector Store
DELETE FROM vector_store
WHERE NOT EXISTS (
    SELECT 1 FROM social_message p
    WHERE p.id = vector_store.message_id
    AND p.create_date_time = vector_store.msg_timestamp
);

-- 4. Clean GIS Info
DELETE FROM gis_info
WHERE NOT EXISTS (
    SELECT 1 FROM social_message p
    WHERE p.id = gis_info.message_id
    AND p.create_date_time = gis_info.msg_timestamp
);