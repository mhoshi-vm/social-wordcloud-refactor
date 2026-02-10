package jp.broadcom.tanzu.mhoshi.social.restapi.stockprice;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
interface StockMetricsRepository extends CrudRepository<StockMetrics, LocalDateTime> {

	@Query("select * from daily_stock_metrics")
	List<StockMetrics> getStockMetrics();

}
