package jp.broadcom.tanzu.mhoshi.social.restapi.stockprice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stocks")
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
