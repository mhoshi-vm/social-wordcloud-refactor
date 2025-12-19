package jp.broadcom.tanzu.mhoshi.socialrestapi.stock;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
class StockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    String ticker;
    Float price;
    Instant updated;
    Integer volume;

    public Long getId() {
        return id;
    }

    public String getTicker() {
        return ticker;
    }

    public Float getPrice() {
        return price;
    }

    public Instant getUpdated() {
        return updated;
    }

    public Integer getVolume() {
        return volume;
    }
}
