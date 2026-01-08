VACUUM ANALYZE;

DO $$
DECLARE
    -- Calculate the start of next month
    next_month_start DATE := date_trunc('month', current_date + interval '1 month');
    -- Calculate the start of the month after next (the range end)
    next_month_end DATE := date_trunc('month', current_date + interval '2 month');
    -- Format names for the partition (e.g., social_message_y2026_m02)
    partition_name TEXT := 'social_message_y' || to_char(next_month_start, 'YYYY_mMM');
BEGIN
    -- Check if the partition already exists to avoid errors
    IF NOT EXISTS (SELECT 1 FROM pg_class WHERE relname = partition_name) THEN
        EXECUTE format(
            'CREATE TABLE %I PARTITION OF social_message
             FOR VALUES FROM (%L) TO (%L)',
            partition_name,
            next_month_start,
            next_month_end
        );
        RAISE NOTICE 'Partition % created.', partition_name;
    ELSE
        RAISE NOTICE 'Partition % already exists. Skipping.', partition_name;
    END IF;
END $$;

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