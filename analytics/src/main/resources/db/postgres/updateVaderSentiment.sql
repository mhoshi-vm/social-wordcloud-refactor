INSERT INTO message_entity_sentiment (message_id, msg_timestamp, model_name, sentiment_label, confidence_score)
SELECT s.id, s.create_date_time,'vader', 'neutral', vader_sentiment(s.text)
FROM social_message s
WHERE NOT EXISTS (
    SELECT 1 FROM message_entity_sentiment m
    WHERE m.message_id = s.id AND m.model_name = 'vader'
);