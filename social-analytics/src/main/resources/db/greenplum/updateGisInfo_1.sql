SELECT *
FROM social_message
WHERE id NOT IN (SELECT message_id FROM gis_info)
ORDER BY create_date_time DESC LIMIT 3;