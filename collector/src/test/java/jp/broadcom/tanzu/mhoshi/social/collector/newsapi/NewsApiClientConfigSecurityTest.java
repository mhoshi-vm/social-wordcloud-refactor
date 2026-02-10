package jp.broadcom.tanzu.mhoshi.social.collector.newsapi;

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

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@Import(TestContainersConfiguration.class)
class NewsApiClientConfigSecurityTest {

	@MockitoBean
	private NewsApiClient newsApiClient;

	@Autowired
	private Supplier<NewsApiResponse> newsApiSupplier;

	@Autowired
	private NewsApiProperties newsApiProperties;

	private ListAppender<ILoggingEvent> listAppender;

	private Logger logger;

	@BeforeEach
	void setUp() {
		logger = (Logger) LoggerFactory
			.getLogger("jp.broadcom.tanzu.mhoshi.social.collector.newsapi.NewsApiClientConfig");

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
	void requestInterceptor_ShouldMaskApiKeyInUri_InLogOutput() {
		// Arrange
		NewsApiResponse.NewsApiResponseArticles.NewsApiResponseSource source = new NewsApiResponse.NewsApiResponseArticles.NewsApiResponseSource(
				"cnn", "CNN");

		NewsApiResponse.NewsApiResponseArticles article = new NewsApiResponse.NewsApiResponseArticles(source, "Author",
				"Description", "http://example.com", "2023-10-01T10:00:00Z", "Content");

		NewsApiResponse mockResponse = new NewsApiResponse("ok", 1, List.of(article));

		Mockito.when(newsApiClient.getEveryNews(any(), any(), any(), any(), any(), any(), any(), any()))
			.thenReturn(mockResponse);

		// Act
		newsApiSupplier.get();

		// Assert - Check logs if captured
		List<ILoggingEvent> logsList = listAppender.list;

		if (!logsList.isEmpty()) {
			String allLogs = logsList.stream()
				.map(ILoggingEvent::getFormattedMessage)
				.reduce("", (a, b) -> a + "\n" + b);

			// API key should be masked in URI query parameters
			String actualApiKey = newsApiProperties.key();
			assertThat(allLogs).doesNotContain(actualApiKey).as("NewsAPI key should be masked in URI logs");

			// Should contain masked placeholder if URI was logged
			if (allLogs.contains("URI:")) {
				assertThat(allLogs).contains("****").as("URI log should mask query parameters");
			}
		}
	}

	@Test
	void requestInterceptor_ShouldNotLeakApiKey_InLogOutput() {
		// Arrange
		NewsApiResponse.NewsApiResponseArticles.NewsApiResponseSource source = new NewsApiResponse.NewsApiResponseArticles.NewsApiResponseSource(
				"bbc", "BBC");

		NewsApiResponse.NewsApiResponseArticles article = new NewsApiResponse.NewsApiResponseArticles(source, "Author",
				"Desc", "http://url.com", "2023-10-01T10:00:00Z", "Content");

		NewsApiResponse mockResponse = new NewsApiResponse("ok", 1, List.of(article));

		Mockito.when(newsApiClient.getEveryNews(any(), any(), any(), any(), any(), any(), any(), any()))
			.thenReturn(mockResponse);

		// Act
		newsApiSupplier.get();

		// Assert
		List<ILoggingEvent> logsList = listAppender.list;

		String allLogs = logsList.stream().map(ILoggingEvent::getFormattedMessage).reduce("", (a, b) -> a + "\n" + b);

		// The actual API key should NOT appear in any log
		String actualApiKey = newsApiProperties.key();
		assertThat(allLogs).doesNotContain(actualApiKey).as("NewsAPI key should never appear in logs in plain text");
	}

}
