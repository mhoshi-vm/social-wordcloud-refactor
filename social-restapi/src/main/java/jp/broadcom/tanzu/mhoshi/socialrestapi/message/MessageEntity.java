package jp.broadcom.tanzu.mhoshi.socialrestapi.message;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_message")
public class MessageEntity {

    @Id
    String id;

    String origin;

    @Column(columnDefinition = "TEXT")
    String text;

    String lang;

    @Column(columnDefinition = "TEXT")
    String name;

    @Column(columnDefinition = "TEXT")
    String url;

    LocalDateTime createDateTime;

    String sentiment;

    Float sentimentScore;

    EventAction action;

    public String getId() {
        return id;
    }

    public String getOrigin() {
        return origin;
    }

    public String getText() {
        return text;
    }

    public String getLang() {
        return lang;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public EventAction getAction() {
        return action;
    }

    public Float getSentimentScore() {
        return sentimentScore;
    }

    public String getSentiment() {
        return sentiment;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }
}
