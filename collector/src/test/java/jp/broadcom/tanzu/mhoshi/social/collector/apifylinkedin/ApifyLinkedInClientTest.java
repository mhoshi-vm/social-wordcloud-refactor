package jp.broadcom.tanzu.mhoshi.social.collector.apifylinkedin;

import jp.broadcom.tanzu.mhoshi.social.collector.TestContainersConfiguration;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.SocialMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
@Import(TestContainersConfiguration.class)
class ApifyLinkedInClientTest {

	@MockitoBean
	private ApifyLinkedInClient apifyLinkedInClient;

	@Autowired
	private Supplier<List<ApifyLinkedInResponse>> pollLinkedIn;

	@Autowired
	private Function<List<ApifyLinkedInResponse>, List<SocialMessage>> convertLinkedInResponse;

	@Autowired
	private ApifyLinkedInProperties apifyLinkedInProperties;

	@Test
	void pollLinkedIn_ShouldFetchResponses() {
		// Arrange
		String appId = apifyLinkedInProperties.appId();
		String token = apifyLinkedInProperties.token();

		ApifyLinkedInResponse.ApifyLinkedInResponseAuthor author = new ApifyLinkedInResponse.ApifyLinkedInResponseAuthor(
				"Broadcom");

		ApifyLinkedInResponse.ApifyLinkedInResponsePostedAt postedAt = new ApifyLinkedInResponse.ApifyLinkedInResponsePostedAt(
				System.currentTimeMillis());

		ApifyLinkedInResponse response = new ApifyLinkedInResponse("activity-1", "http://linkedin.com/post/1",
				"Hello World", author, postedAt);

		Mockito.when(apifyLinkedInClient.apifyLinkedInResponses(eq(appId), eq(token), any(ApifyLinkedInRequest.class)))
			.thenReturn(List.of(response));

		// Act
		List<ApifyLinkedInResponse> result = pollLinkedIn.get();

		// Assert
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().activity_id()).isEqualTo("activity-1");

		// Verify the client was called with the correct request parameters based on
		// properties
		Mockito.verify(apifyLinkedInClient)
			.apifyLinkedInResponses(eq(appId), eq(token), eq(new ApifyLinkedInRequest(apifyLinkedInProperties.keyword(),
					apifyLinkedInProperties.pollingLimit(), apifyLinkedInProperties.sortType())));
	}

	@Test
	void convertLinkedInResponse_ShouldConvertToSocialMessage() {
		// Arrange
		long timestamp = 1696156800000L; // 2023-10-01T10
		ApifyLinkedInResponse.ApifyLinkedInResponseAuthor author = new ApifyLinkedInResponse.ApifyLinkedInResponseAuthor(
				"Jane Doe");

		ApifyLinkedInResponse.ApifyLinkedInResponsePostedAt postedAt = new ApifyLinkedInResponse.ApifyLinkedInResponsePostedAt(
				timestamp);

		ApifyLinkedInResponse response = new ApifyLinkedInResponse("urn:li:activity:12345",
				"https://www.linkedin.com/feed/update/urn:li:activity:12345", "This is a LinkedIn post content.",
				author, postedAt);

		// Act
		List<SocialMessage> messages = convertLinkedInResponse.apply(List.of(response));

		// Assert
		assertThat(messages).hasSize(1);
		SocialMessage msg = messages.getFirst();

		assertThat(msg.id()).isEqualTo("urn:li:activity:12345");
		assertThat(msg.origin()).isEqualTo("linkedIn");
		assertThat(msg.text()).isEqualTo("This is a LinkedIn post content.");
		assertThat(msg.lang()).isEqualTo("en"); // Hardcoded in Config
		assertThat(msg.name()).isEqualTo("Jane Doe");
		assertThat(msg.url()).isEqualTo("https://www.linkedin.com/feed/update/urn:li:activity:12345");

		LocalDateTime expectedTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
		assertThat(msg.createDateTime()).isEqualTo(expectedTime);
	}

}