package jp.broadcom.tanzu.mhoshi.socialcollector.stocksapi;

import java.time.Instant;

record StockPriceResponse(
        String ticker,
        Float price,
        Instant updated,
        Integer volume){
}
