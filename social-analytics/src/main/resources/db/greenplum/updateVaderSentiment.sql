INSERT INTO message_entity_sentiment (message_id, msg_timestamp, model_name, sentiment_label, confidence_score)
SELECT id, create_date_time,'vader', sentiment_data->>'label', (sentiment_data->>'score')::numeric
FROM (
    SELECT id, create_date_time, vader_sentiment(text) AS sentiment_data
      FROM social_message s
      WHERE NOT EXISTS (SELECT 1
                        FROM message_entity_sentiment m
                        WHERE m.message_id = s.id
                          AND m.model_name = 'vader')
) AS subquery
ON CONFLICT (message_id, msg_timestamp) DO
UPDATE
SET model_name = EXCLUDED.model_name, sentiment_label = EXCLUDED.sentiment_label, confidence_score = EXCLUDED.confidence_score;