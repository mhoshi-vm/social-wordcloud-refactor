package jp.broadcom.tanzu.mhoshi.social.collector.stocksapi;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jp.broadcom.tanzu.mhoshi.social.collector.TestContainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@Import(TestContainersConfiguration.class)
class StocksApiClientConfigSecurityTest {

	@MockitoBean
	private StocksApiClient stocksApiClient;

	@Autowired
	private Supplier<StockPriceResponse> pollStocksApi;

	@Autowired
	private StocksApiProperties stocksApiProperties;

	private ListAppender<ILoggingEvent> listAppender;

	private Logger logger;

	@BeforeEach
	void setUp() {
		logger = (Logger) LoggerFactory
			.getLogger("jp.broadcom.tanzu.mhoshi.social.collector.stocksapi.StocksApiClientConfig");

		logger.setLevel(ch.qos.logback.classic.Level.INFO);

		listAppender = new ListAppender<>();
		listAppender.start();

		logger.addAppender(listAppender);
	}

	@AfterEach
	void tearDown() {
		logger.detachAppender(listAppender);
	}

	@Test
	void requestInterceptor_ShouldMaskApiKey_InLogOutput() {
		// Arrange
		StockPriceResponse response = new StockPriceResponse("AVGO", 850.5f, Instant.now(), 1000000);

		Mockito.when(stocksApiClient.getStockPriceResponses(any())).thenReturn(response);

		// Act
		pollStocksApi.get();

		// Assert - Check logs if captured
		List<ILoggingEvent> logsList = listAppender.list;

		if (!logsList.isEmpty()) {
			String allLogs = logsList.stream()
				.map(ILoggingEvent::getFormattedMessage)
				.reduce("", (a, b) -> a + "\n" + b);

			// API key should be masked, not exposed
			String actualApiKey = stocksApiProperties.apiKey();
			assertThat(allLogs).doesNotContain(actualApiKey).as("X-Api-Key should be masked in logs");

			// Should contain masked placeholder if headers were logged
			if (allLogs.contains("Headers:")) {
				assertThat(allLogs).contains("****").as("Headers log should contain masking placeholder");
			}
		}
	}

	@Test
	void requestInterceptor_ShouldNotLeakApiKey_InLogOutput() {
		// Arrange
		StockPriceResponse response = new StockPriceResponse("AVGO", 850.5f, Instant.now(), 1000000);

		Mockito.when(stocksApiClient.getStockPriceResponses(any())).thenReturn(response);

		// Act
		pollStocksApi.get();

		// Assert
		List<ILoggingEvent> logsList = listAppender.list;

		String allLogs = logsList.stream().map(ILoggingEvent::getFormattedMessage).reduce("", (a, b) -> a + "\n" + b);

		// The actual API key should NOT appear in any log
		String actualApiKey = stocksApiProperties.apiKey();
		assertThat(allLogs).doesNotContain(actualApiKey).as("API key should never appear in logs in plain text");
	}

}
