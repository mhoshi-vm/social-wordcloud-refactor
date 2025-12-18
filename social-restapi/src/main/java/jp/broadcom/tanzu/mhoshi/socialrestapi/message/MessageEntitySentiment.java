package jp.broadcom.tanzu.mhoshi.socialrestapi.message;

import jakarta.persistence.*;

@Entity
class MessageEntitySentiment {
    @Id
    Long id;

    String modelName; // e.g., "VADER", "BERT", "GPT-4"

    String sentimentLabel; // "POSITIVE", "NEUTRAL"

    Float confidenceScore; // 0.95

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id") // This creates the Foreign Key column in this table
    MessageEntity message;

    String getModelName() {
        return modelName;
    }

    String getSentimentLabel() {
        return sentimentLabel;
    }

    Float getConfidenceScore() {
        return confidenceScore;
    }
}
