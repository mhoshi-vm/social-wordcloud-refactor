VACUUM ANALYZE;

--- Update gisCentroids
DO $$
DECLARE
    row_count INTEGER;
BEGIN
    -- Replace 'your_table_name' with the actual table you are monitoring
    SELECT count(*) INTO row_count FROM gis_info;

    IF row_count >= 5 THEN
        PERFORM train_and_refresh_clusters();
        RAISE NOTICE 'Condition met: Function executed.';
    ELSE
        RAISE NOTICE 'Condition not met: Only % rows found.', row_count;
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

DO $$
DECLARE
    -- 1. Timing Calculations
    -- Calculate start of next month (e.g., if today is Oct 15, this is Nov 01)
    next_month_start TIMESTAMPTZ := date_trunc('month', current_date + interval '1 month');
    -- Calculate end of next month (e.g., Dec 01)
    next_month_end   TIMESTAMPTZ := date_trunc('month', current_date + interval '2 month');

    -- 2. Suffix Generation
    -- Creates a consistent suffix like '_y2026_m02'
    partition_suffix TEXT := '_y' || to_char(next_month_start, 'YYYY_mMM');

    -- 3. Configuration
    -- Array of all tables that need partitions
    target_tables TEXT[] := ARRAY[
        'social_message',
        'message_entity_sentiment',
        'message_entity_tsvector',
        'vector_store',
        'gis_info'
    ];

    -- Variables for the loop
    t_name    TEXT;
    part_name TEXT;
BEGIN
    -- 4. Iterate over every table in the list
    FOREACH t_name IN ARRAY target_tables
    LOOP
        -- Construct the specific partition name (e.g., vector_store_y2026_m02)
        part_name := t_name || partition_suffix;

        -- Check if partition exists
        IF NOT EXISTS (SELECT 1 FROM pg_class WHERE relname = part_name) THEN

            -- Execute creation
            EXECUTE format(
                'CREATE TABLE %I PARTITION OF %I
                 FOR VALUES FROM (%L) TO (%L)',
                part_name,        -- The new partition name
                t_name,           -- The parent table
                next_month_start, -- Start Date
                next_month_end    -- End Date
            );

            RAISE NOTICE 'Created partition: % (Range: % to %)', part_name, next_month_start, next_month_end;

        ELSE
            RAISE NOTICE 'Partition % already exists. Skipping.', part_name;
        END IF;
    END LOOP;

END $$;