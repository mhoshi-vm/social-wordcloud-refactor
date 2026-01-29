package jp.broadcom.tanzu.mhoshi.socialwebapi.stockprice;

import java.time.LocalDateTime;

class StockMetrics {
    LocalDateTime bucket;
    String ticker;
    Float avgPrice;

    public LocalDateTime getBucket() {
        return bucket;
    }

    public void setBucket(LocalDateTime bucket) {
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
