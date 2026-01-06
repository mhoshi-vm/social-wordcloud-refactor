import json
import nltk
if 'nltk_initialized' not in GD:
    nltk.download('vader_lexicon')
    GD['nltk_initialized'] = True
from nltk.sentiment.vader import SentimentIntensityAnalyzer

sid = SentimentIntensityAnalyzer()
scores = sid.polarity_scores(text)
compound = scores['compound']

if compound >= 0.05:
    label = 'positive'
elif compound <= -0.05:
    label = 'negative'
else:
    label = 'neutral'

return json.dumps({"label": label, "score": compound})
