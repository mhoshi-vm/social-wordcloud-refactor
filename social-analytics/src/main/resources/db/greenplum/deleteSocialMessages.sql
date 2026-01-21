WITH targets AS (
    -- 1. Select the ID and Timestamp for the IDs you want to delete
    SELECT id, create_date_time
    FROM social_message
    WHERE id IN (?))
-- 2. Aggregate them into arrays and call the function
SELECT delete_social_message_batch(
               ARRAY (SELECT id FROM targets), -- p_message_ids
               ARRAY (SELECT create_date_time FROM targets) -- p_timestamps
       );