UPDATE social_message
SET sentiment_score=vader_sentiment(text)  WHERE sentiment_score IS NULL