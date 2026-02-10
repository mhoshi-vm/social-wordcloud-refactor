package jp.broadcom.tanzu.mhoshi.social.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsAiServiceTest {

	@Mock
	private ChatModel chatModel;

	@Mock
	private EmbeddingModel embeddingModel;

	// Mocks for ChatClient fluent API
	@Mock
	private ChatClient.Builder chatClientBuilder;

	@Mock
	private ChatClient chatClient;

	@Mock
	private ChatClient.ChatClientRequestSpec requestSpec;

	@Mock
	private ChatClient.CallResponseSpec responseSpec;

	private AnalyticsAiService analyticsAiService;

	@BeforeEach
	void setUp() {
		// Mock the static creation of ChatClient
		// Note: In a real Spring Boot test, you might use @MockBean.
		// For pure unit testing, we need to handle how ChatClient.create(chatModel) is
		// called.
		// However, since ChatClient.create is static, it is hard to mock with standard
		// Mockito.
		// A better approach for unit testing is to refactor the constructor or use a
		// Builder pattern in the test.
		// Assuming we can't change the source, we will mock the behavior if we inject the
		// ChatClient directly
		// OR we just mock the ChatModel if ChatClient wraps it simply.

		// *Correction*: Since `AnalyticsAiService` calls `ChatClient.create(chatModel)`
		// inside the constructor,
		// we cannot easily mock the resulting `ChatClient` without using Mockito-inline
		// (static mocking)
		// or refactoring the code to accept `ChatClient` in the constructor.

		// *Recommended Refactor for Testability*:
		// Ideally, AnalyticsAiService should take ChatClient as a constructor argument.
		// For this test, we will assume we can mock the internal ChatClient or use a
		// partial mock.
		// BUT, given the provided code, it calls `ChatClient.create(chatModel)`.

		// Workaround for this specific test structure without changing source:
		// We will focus on testing the Logic assuming ChatClient works,
		// OR we use a SpringBootTest slice to let Spring inject the ChatClient builder.

		// Let's assume for this example we are writing a "Slice Test" or we refactor
		// slightly.
		// If we strictly cannot change code, we need to rely on the underlying ChatModel
		// behavior
		// or use PowerMock (discouraged).

		// Strategy: Use a real ChatClient with a Mock ChatModel if possible,
		// OR standard approach: Testing `getEmbeddingResponse` is easy. `getGisInfo` is
		// harder.

		// Let's test `getEmbeddingResponse` first as it directly uses `embeddingModel`.
		analyticsAiService = new AnalyticsAiService(chatModel, embeddingModel);

		// Reflection to inject our mock ChatClient for `getGisInfo` testing
		// This avoids the static method issue for the purpose of this test.
		try {
			java.lang.reflect.Field chatClientField = AnalyticsAiService.class.getDeclaredField("chatClient");
			chatClientField.setAccessible(true);
			chatClientField.set(analyticsAiService, chatClient);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void getEmbeddingResponse_ShouldReturnEmbeddings() {
		// Arrange
		List<String> texts = List.of("Test message 1", "Test message 2");
		EmbeddingResponse expectedResponse = new EmbeddingResponse(
				List.of(new Embedding(new float[] { 0.1f, 0.2f }, 0)));

		when(embeddingModel.embedForResponse(texts)).thenReturn(expectedResponse);

		// Act
		EmbeddingResponse actualResponse = analyticsAiService.getEmbeddingResponse(texts);

		// Assert
		assertThat(actualResponse).isNotNull();
		assertThat(actualResponse).isEqualTo(expectedResponse);
		verify(embeddingModel).embedForResponse(texts);
	}

	@Test
	void getGisInfo_ShouldReturnListOfGisInfo() {
		// Arrange
		String messageText = "Earthquake in Tokyo";
		List<String> messages = List.of(messageText);
		GisInfo expectedGis = new GisInfo("123", null, 4326, "POINT(35.68 139.76)", "Located based on keyword Tokyo");

		// Mocking the Fluent API chain: chatClient.prompt().user(...).call().entity(...)
		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user(any(java.util.function.Consumer.class))).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
		when(responseSpec.entity(GisInfo.class)).thenReturn(expectedGis);

		// Act
		List<GisInfo> result = analyticsAiService.getGisInfo(messages);

		// Assert
		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(expectedGis);
		assertThat(result.get(0).gis()).isEqualTo("POINT(35.68 139.76)");
	}

	@Test
	void getGisInfo_ShouldHandleNullResponse() {
		// Arrange
		List<String> messages = List.of("Unknown location");

		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user(any(java.util.function.Consumer.class))).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
		when(responseSpec.entity(GisInfo.class)).thenReturn(null); // AI returned nothing

		// Act
		List<GisInfo> result = analyticsAiService.getGisInfo(messages);

		// Assert
		assertThat(result).isEmpty();
	}

}