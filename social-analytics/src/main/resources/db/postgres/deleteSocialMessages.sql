WITH targets AS (
    SELECT id
    FROM social_message
    WHERE id IN ( ? )
    -- Example of useful CTE logic:
    -- AND create_date_time < '2023-01-01'
)
DELETE FROM social_message
WHERE id IN (SELECT id FROM targets);