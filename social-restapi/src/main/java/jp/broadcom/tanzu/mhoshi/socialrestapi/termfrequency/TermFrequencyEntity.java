package jp.broadcom.tanzu.mhoshi.socialrestapi.termfrequency;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
class TermFrequencyEntity {

    @Id
    Integer rank;

    String term;

    Integer count;

    public Integer getRank() {
        return rank;
    }

    public String getTerm() {
        return term;
    }

    public Integer getCount() {
        return count;
    }

}
