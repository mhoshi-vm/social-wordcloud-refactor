package jp.broadcom.tanzu.mhoshi.social.collector.mastodon;

import ch.qos.logback.classic.Level;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@Import(TestContainersConfiguration.class)
class MastodonClientConfigSecurityTest {

	@MockitoBean
	private MastodonClient mastodonClient;

	@Autowired
	private Supplier<List<MastodonTimelinesResponse>> pollMastodon;

	@Autowired
	private MastodonProperties mastodonProperties;

	private ListAppender<ILoggingEvent> listAppender;

	private Logger logger;

	@BeforeEach
	void setUp() {
		// Get the logger for MastodonClientConfig
		logger = (Logger) LoggerFactory
			.getLogger("jp.broadcom.tanzu.mhoshi.social.collector.mastodon.MastodonClientConfig");

		// Create and start a ListAppender
		listAppender = new ListAppender<>();
		listAppender.start();

		// Add appender to logger
		logger.addAppender(listAppender);
	}

	@AfterEach
	void tearDown() {
		// Clean up
		logger.detachAppender(listAppender);
	}

	@Test
	void requestInterceptor_ShouldMaskAuthorizationToken_InLogOutput() {
		// Arrange
		String hashtag = mastodonProperties.hashTag();
		int limit = mastodonProperties.pollingLimit();

		MastodonTimelinesResponse.MastodonTimelinesResponseAccount account = new MastodonTimelinesResponse.MastodonTimelinesResponseAccount(
				"user", "User Name");

		MastodonTimelinesResponse response = new MastodonTimelinesResponse("100", "Content", "en", "http://url",
				LocalDateTime.now(), account);

		Mockito.when(mastodonClient.getMastodonTimeLineResponses(eq(hashtag), eq(limit), any(), any()))
			.thenReturn(List.of(response));

		// Act
		pollMastodon.get();

		// Assert - Check that logs exist
		List<ILoggingEvent> logsList = listAppender.list;
		assertThat(logsList).isNotEmpty();

		// Find the log entry that contains headers
		ILoggingEvent headersLog = logsList.stream()
			.filter(event -> event.getFormattedMessage().contains("Headers:"))
			.findFirst()
			.orElseThrow(() -> new AssertionError("No headers log found"));

		String logMessage = headersLog.getFormattedMessage();

		// Assert - Token should be masked, not exposed
		String actualToken = mastodonProperties.token();
		assertThat(logMessage).doesNotContain(actualToken)
			.as("Authorization token should be masked in logs, but found the actual token: %s", actualToken);

		// Assert - Should contain masked placeholder
		assertThat(logMessage).containsAnyOf("***", "****", "[MASKED]", "[REDACTED]")
			.as("Headers log should contain a masking placeholder like *** or [MASKED]");
	}

	@Test
	void requestInterceptor_ShouldNotLeakBearerToken_InLogOutput() {
		// Arrange
		MastodonTimelinesResponse.MastodonTimelinesResponseAccount account = new MastodonTimelinesResponse.MastodonTimelinesResponseAccount(
				"user", "User");

		MastodonTimelinesResponse response = new MastodonTimelinesResponse("100", "Content", "en", "http://url",
				LocalDateTime.now(), account);

		Mockito.when(mastodonClient.getMastodonTimeLineResponses(any(), any(), any(), any()))
			.thenReturn(List.of(response));

		// Act
		pollMastodon.get();

		// Assert
		List<ILoggingEvent> logsList = listAppender.list;

		// Check all log messages
		String allLogs = logsList.stream().map(ILoggingEvent::getFormattedMessage).reduce("", (a, b) -> a + "\n" + b);

		// The actual bearer token value should NOT appear in any log
		String actualToken = mastodonProperties.token();
		assertThat(allLogs).doesNotContain(actualToken).as("Bearer token should never appear in logs in plain text");
	}

}
