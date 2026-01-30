package jp.broadcom.tanzu.mhoshi.social.collector.stocksapi;

import java.time.Instant;

record StockPriceResponse(
        String ticker,
        Float price,
        Instant updated,
        Integer volume){
}
