package jp.broadcom.tanzu.mhoshi.socialwebapi.stockprice;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface StockMetricsRepository {

    @Query("select * from daily_stock_metrics")
    List<StockMetrics> getStockMetrics();
}
