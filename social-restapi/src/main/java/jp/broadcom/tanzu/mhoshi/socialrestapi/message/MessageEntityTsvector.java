package jp.broadcom.tanzu.mhoshi.socialrestapi.message;

import jakarta.persistence.*;

@Entity
class MessageEntityTsvector {

    @Id
    Long id;

    @Column(name = "word_vector", columnDefinition = "tsvector")
    String wordVector; // e.g., "VADER", "BERT", "GPT-4"

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id") // This creates the Foreign Key column in this table
    MessageEntity message;
}
