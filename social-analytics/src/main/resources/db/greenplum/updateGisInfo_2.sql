INSERT INTO gis_info (message_id, msg_timestamp, srid, gis, reason)
VALUES (?, ?, ?, ?, ?)
ON CONFLICT (message_id, msg_timestamp) DO
UPDATE
SET srid = EXCLUDED.srid, gis = EXCLUDED.gis, reason = EXCLUDED.reason;