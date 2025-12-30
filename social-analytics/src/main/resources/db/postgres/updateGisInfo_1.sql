SELECT * FROM social_message
WHERE id NOT IN (SELECT message_id FROM gis_info)
LIMIT 3