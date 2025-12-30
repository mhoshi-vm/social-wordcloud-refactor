INSERT INTO gis_info (message_id, srid, gis, reason)
VALUES (?, ?, ?, ?) ON CONFLICT (message_id) DO
UPDATE SET gis = EXCLUDED.gis, reason = EXCLUDED.reason;