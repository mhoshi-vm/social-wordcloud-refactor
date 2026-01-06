package jp.broadcom.tanzu.mhoshi.socialcollector.stocksapi;

import org.springframework.resilience.annotation.Retryable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange
interface StocksApiClient {

    @Retryable(maxRetries = 1)
    @GetExchange(url="/v1/stockprice", accept = "application/json")
    StockPriceResponse getStockPriceResponses(
            @RequestParam String ticker);
}