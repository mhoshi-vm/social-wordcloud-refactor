package jp.broadcom.tanzu.mhoshi.social.collector.stocksapi;

import jp.broadcom.tanzu.mhoshi.social.collector.TestContainersConfiguration;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.SocialMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
@Import(TestContainersConfiguration.class)
class StocksApiClientTest {

	@MockitoBean
	private StocksApiClient stocksApiClient;

	@Autowired
	private Supplier<StockPriceResponse> pollStocksApi;

	@Autowired
	private Function<StockPriceResponse, List<SocialMessage>> convertStocksApi;

	@Autowired
	private StocksApiProperties stocksApiProperties;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void pollStocksApi_ShouldReturnResponse() {
		// Arrange
		String ticker = stocksApiProperties.ticker();
		StockPriceResponse mockResponse = new StockPriceResponse(ticker, 150.0f, Instant.now(), 1000);

		Mockito.when(stocksApiClient.getStockPriceResponses(eq(ticker))).thenReturn(mockResponse);

		// Act
		StockPriceResponse result = pollStocksApi.get();

		// Assert
		assertThat(result).isEqualTo(mockResponse);
		assertThat(result.ticker()).isEqualTo(ticker);
	}

	@Test
	void convertStocksApi_ShouldSerializeResponseToJson() throws JsonProcessingException {
		// Arrange
		Instant updated = Instant.parse("2023-10-01T10:00:00Z");
		StockPriceResponse response = new StockPriceResponse("AAPL", 150.50f, updated, 5000);

		// Act
		List<SocialMessage> messages = convertStocksApi.apply(response);

		// Assert
		assertThat(messages).hasSize(1);
		SocialMessage msg = messages.getFirst();

		// Check ID generation logic: "stocksprice:" + updated
		String expectedId = UUID.nameUUIDFromBytes(("stocksprice:" + updated).getBytes()).toString();
		assertThat(msg.id()).isEqualTo(expectedId);

		assertThat(msg.origin()).isEqualTo("stocksprice");

		// Content should be the JSON representation
		String jsonContent = msg.text();
		StockPriceResponse deserialized = objectMapper.readValue(jsonContent, StockPriceResponse.class);
		assertThat(deserialized.price()).isEqualTo(150.50f);

		assertThat(msg.name()).isEqualTo(stocksApiProperties.ticker());
	}

}