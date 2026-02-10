package jp.broadcom.tanzu.mhoshi.social.collector.shared;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.client.MockClientHttpRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataMaskerTest {

	@Test
	void maskHeaders_ShouldMaskAuthorizationHeader() {
		// Arrange
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer secret-token-12345");
		headers.set("Content-Type", "application/json");

		// Act
		String masked = SensitiveDataMasker.maskHeaders(headers);

		// Assert
		assertThat(masked).contains("Authorization:****");
		assertThat(masked).doesNotContain("secret-token-12345");
		assertThat(masked).contains("Content-Type:[application/json]");
	}

	@Test
	void maskHeaders_ShouldMaskXApiKeyHeader() {
		// Arrange
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Api-Key", "secret-key-67890");
		headers.set("User-Agent", "test-agent");

		// Act
		String masked = SensitiveDataMasker.maskHeaders(headers);

		// Assert
		assertThat(masked).contains("X-Api-Key:****");
		assertThat(masked).doesNotContain("secret-key-67890");
		assertThat(masked).contains("User-Agent:[test-agent]");
	}

	@Test
	void maskHeaders_ShouldHandleEmptyHeaders() {
		// Arrange
		HttpHeaders headers = new HttpHeaders();

		// Act
		String masked = SensitiveDataMasker.maskHeaders(headers);

		// Assert
		assertThat(masked).isEqualTo("[]");
	}

	@Test
	void maskHeaders_ShouldHandleNullHeaders() {
		// Act
		String masked = SensitiveDataMasker.maskHeaders(null);

		// Assert
		assertThat(masked).isEqualTo("[]");
	}

	@Test
	void maskUri_ShouldMaskApiKeyQueryParameter() {
		// Arrange
		URI uri = URI.create("https://api.example.com/v2/data?apiKey=secret123&limit=10");

		// Act
		String masked = SensitiveDataMasker.maskUri(uri);

		// Assert
		assertThat(masked).contains("apiKey=****");
		assertThat(masked).doesNotContain("secret123");
		assertThat(masked).contains("limit=10");
	}

	@Test
	void maskUri_ShouldMaskKeyQueryParameter() {
		// Arrange
		URI uri = URI.create("https://newsapi.org/v2/everything?key=abc123def&pageSize=100");

		// Act
		String masked = SensitiveDataMasker.maskUri(uri);

		// Assert
		assertThat(masked).contains("key=****");
		assertThat(masked).doesNotContain("abc123def");
		assertThat(masked).contains("pageSize=100");
	}

	@Test
	void maskUri_ShouldHandleUriWithoutQueryParams() {
		// Arrange
		URI uri = URI.create("https://api.example.com/v1/posts");

		// Act
		String masked = SensitiveDataMasker.maskUri(uri);

		// Assert
		assertThat(masked).isEqualTo("https://api.example.com/v1/posts");
	}

	@Test
	void maskUri_ShouldHandleNullUri() {
		// Act
		String masked = SensitiveDataMasker.maskUri(null);

		// Assert
		assertThat(masked).isEmpty();
	}

	@Test
	void maskRequest_ShouldMaskBothUriAndHeaders() {
		// Arrange
		MockClientHttpRequest request = new MockClientHttpRequest();
		request.setURI(URI.create("https://api.test.com/data?apiKey=secret"));
		request.getHeaders().set("Authorization", "Bearer token123");
		request.getHeaders().set("Accept", "application/json");

		// Act
		String masked = SensitiveDataMasker.maskRequest(request);

		// Assert
		assertThat(masked).contains("apiKey=****");
		assertThat(masked).doesNotContain("secret");
		assertThat(masked).contains("Authorization:****");
		assertThat(masked).doesNotContain("token123");
		assertThat(masked).contains("Accept:[application/json]");
	}

	@Test
	void maskHeaders_ShouldMaskApiKeyHeader_CaseInsensitive() {
		// Arrange
		HttpHeaders headers = new HttpHeaders();
		headers.set("api-key", "sensitive-key");
		headers.set("API-KEY", "another-key");

		// Act
		String masked = SensitiveDataMasker.maskHeaders(headers);

		// Assert
		assertThat(masked).contains("api-key:****");
		assertThat(masked).contains("API-KEY:****");
		assertThat(masked).doesNotContain("sensitive-key");
		assertThat(masked).doesNotContain("another-key");
	}

	@Test
	void maskUri_ShouldMaskApiKeyWithUnderscore() {
		// Arrange
		URI uri = URI.create("https://api.example.com/endpoint?api_key=secret999&other=value");

		// Act
		String masked = SensitiveDataMasker.maskUri(uri);

		// Assert
		assertThat(masked).contains("api_key=****");
		assertThat(masked).doesNotContain("secret999");
		assertThat(masked).contains("other=value");
	}

}
