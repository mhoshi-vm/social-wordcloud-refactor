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

		// Assert - Check that logs exist
		List<ILoggingEvent> logsList = listAppender.list;
		assertThat(logsList).isNotEmpty();

		// Find the log entry that contains headers
		ILoggingEvent headersLog = logsList.stream()
			.filter(event -> event.getFormattedMessage().contains("Headers:"))
			.findFirst()
			.orElseThrow(() -> new AssertionError("No headers log found"));

		String logMessage = headersLog.getFormattedMessage();

		// Assert - API key should be masked, not exposed
		String actualApiKey = stocksApiProperties.apiKey();
		assertThat(logMessage).doesNotContain(actualApiKey)
			.as("X-Api-Key should be masked in logs, but found the actual key: %s", actualApiKey);

		// Assert - Should contain masked placeholder
		assertThat(logMessage).containsAnyOf("***", "****", "[MASKED]", "[REDACTED]")
			.as("Headers log should contain a masking placeholder");
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
