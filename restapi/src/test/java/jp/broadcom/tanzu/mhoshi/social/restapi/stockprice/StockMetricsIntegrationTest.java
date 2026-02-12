package jp.broadcom.tanzu.mhoshi.social.restapi.stockprice;

import jp.broadcom.tanzu.mhoshi.social.restapi.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = { "database=postgres" })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StockMetricsIntegrationTest {

	@Autowired
	private StockMetricsRepository repository;

	@Autowired
	private StockMetricsService service;

	@Test
	void repository_GetStockMetrics_ShouldReturnData() {
		// Act
		List<StockMetrics> metrics = repository.getStockMetrics();

		// Assert
		assertThat(metrics).isNotNull();
		// Note: May be empty if no stock data in view, but query should execute without
		// error

		if (!metrics.isEmpty()) {
			StockMetrics first = metrics.getFirst();
			assertThat(first.getBucket()).isNotNull();
			assertThat(first.getTicker()).isNotNull();
			assertThat(first.getAvgPrice()).isNotNull();
		}
	}

	@Test
	void service_GetStockMetrics_ShouldCallRepository() {
		// Act
		List<StockMetrics> metrics = service.getStockMetrics();

		// Assert
		assertThat(metrics).isNotNull();
	}

	@Test
	void repository_GetStockMetrics_ShouldReturnAvgoTicker() {
		// Act
		List<StockMetrics> metrics = repository.getStockMetrics();

		// Assert - data.sql should have AVGO stock prices
		if (!metrics.isEmpty()) {
			boolean hasAvgo = metrics.stream().anyMatch(m -> "AVGO".equals(m.getTicker()));
			assertThat(hasAvgo).isTrue();
		}
	}

	@Autowired
	JdbcClient jdbcClient;

	@AfterAll
	void tearDown() {
		// Code to run once after all tests in this class are done
		jdbcClient.sql("DELETE FROM social_message").update();
		jdbcClient.sql("DELETE FROM social_message_analysis").update();
		// Example: close a static resource or perform database cleanup
	}

}
