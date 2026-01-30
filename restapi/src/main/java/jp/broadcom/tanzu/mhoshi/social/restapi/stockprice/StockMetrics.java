package jp.broadcom.tanzu.mhoshi.social.restapi.stockprice;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;


class StockMetrics {
    @Id
    String bucket;
    String ticker;
    Float avgPrice;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Float getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(Float avgPrice) {
        this.avgPrice = avgPrice;
    }
}
