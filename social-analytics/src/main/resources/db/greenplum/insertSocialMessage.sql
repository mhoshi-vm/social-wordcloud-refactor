INSERT INTO social_message (id, origin, text, lang, name, url, create_date_time)
VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id, create_date_time) DO
UPDATE
SET origin = EXCLUDED.origin, text = EXCLUDED.text, lang = EXCLUDED.lang, name = EXCLUDED.name, url = EXCLUDED.url;