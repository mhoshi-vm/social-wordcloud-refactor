CREATE EXTENSION IF NOT EXISTS pg_cron;

CREATE TABLE syslog_entries (
    created_at TIMESTAMPTZ NOT NULL, -- RFC 5424 uses high-precision timestamps
    priority INTEGER,                -- The PRI value (Facility * 8 + Severity)
    source_host TEXT,
    app_name TEXT,
    proc_id TEXT,
    msg_id TEXT,                     -- RFC 5424 specific: Message ID
    structured_data JSONB,           -- RFC 5424 specific: Key-Value pairs
    log_content TEXT
    -- Add constraints/primary keys as needed for your partitioning
)
WITH (
    appendonly = true,
    orientation = column,
    compresstype = zstd,
    compresslevel = 3
)
DISTRIBUTED RANDOMLY
PARTITION BY RANGE (created_at);

CREATE INDEX idx_syslog_host ON syslog_entries (source_host);

CREATE OR REPLACE FUNCTION create_syslog_partition_next_week()
RETURNS void AS $$
DECLARE
    -- Calculate the start of next week (Monday)
    next_week_start date := date_trunc('week', now()) + interval '1 week';
    -- Calculate end of next week
    next_week_end date := next_week_start + interval '1 week';
    -- Name the partition based on Year and ISO Week number
    partition_name text := 'syslog_entries_' || to_char(next_week_start, 'YYYY_IW');
BEGIN
    -- Execute the creation SQL dynamically
    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF syslog_entries FOR VALUES FROM (%L) TO (%L)',
        partition_name,
        next_week_start,
        next_week_end
    );

    RAISE NOTICE 'Partition % created for range % to %', partition_name, next_week_start, next_week_end;
END;
$$ LANGUAGE plpgsql;

SELECT cron.schedule(
    'create_next_partition_job', -- Job Name
    '0 10 * * 5',                -- Cron schedule (Friday at 10am)
    $$SELECT create_syslog_partition_next_week()$$
);

SELECT create_syslog_partition_next_week();
