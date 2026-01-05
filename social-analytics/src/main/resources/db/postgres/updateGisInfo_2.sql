INSERT INTO gis_info (message_id, msg_timestamp, srid, gis, reason)
VALUES (?, ?, ?, ?, ?) ON CONFLICT (message_id) DO
UPDATE SET msg_timestamp = EXCLUDED.msg_timestamp, gis = EXCLUDED.gis, reason = EXCLUDED.reason;