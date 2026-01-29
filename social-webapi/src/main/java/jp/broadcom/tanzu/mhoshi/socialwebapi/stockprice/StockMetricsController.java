package jp.broadcom.tanzu.mhoshi.socialwebapi.stockprice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class StockMetricsController {

    StockMetricsService stockMetricsService;
    StockMetricsController(StockMetricsService stockMetricsService) {
        this.stockMetricsService = stockMetricsService;
    }

    @GetMapping
    List<StockMetrics> getStockMetrics() {
        return stockMetricsService.getStockMetrics();
    }

}
