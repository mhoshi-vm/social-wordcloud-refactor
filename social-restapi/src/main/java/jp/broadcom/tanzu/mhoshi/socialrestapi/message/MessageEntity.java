package jp.broadcom.tanzu.mhoshi.socialrestapi.message;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "social_message")
class MessageEntity {

    @Id
    String id;

    String origin;

    String text;

    String lang;

    @Column(columnDefinition = "TEXT")
    String name;

    @Column(columnDefinition = "TEXT")
    String url;

    LocalDateTime createDateTime;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    List<MessageEntitySentiment> sentiments;

    @OneToOne(mappedBy = "message", cascade = CascadeType.ALL)
    MessageEntityTsvector Lexemes;

    // The reason for visibility https://www.baeldung.com/jackson-field-serializable-deserializable-or-not
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

    List<MessageEntitySentiment> getSentiments() {
        return sentiments;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    @Override
    public String toString() {
        return "MessageEntity{" + "id='" + id + '\'' +
                ", origin='" + origin + '\'' +
                ", text='" + text + '\'' +
                ", lang='" + lang + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", createDateTime=" + createDateTime +
                ", sentiments='" + sentiments + '\'' +
                '}';
    }
}
