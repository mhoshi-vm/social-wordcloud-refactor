package jp.broadcom.tanzu.mhoshi.social.restapi.stockprice;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class StockMetricsService {

	StockMetricsRepository stockMetricsRepository;

	StockMetricsService(StockMetricsRepository stockMetricsRepository) {
		this.stockMetricsRepository = stockMetricsRepository;
	}

	List<StockMetrics> getStockMetrics() {
		return stockMetricsRepository.getStockMetrics();
	}

}
