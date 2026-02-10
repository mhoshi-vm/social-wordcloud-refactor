package jp.broadcom.tanzu.mhoshi.social.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.Embedding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestContainersConfiguration.class)
// Force the use of Postgres scripts to match the TestContainer environment
@ContextConfiguration(classes = TestAsyncConfig.class)
@TestPropertySource(properties = { "database=postgres", "analytics.database=postgres", "spring.sql.init.mode=always",
		"spring.sql.init.schema-locations=classpath*:db/postgres/schema.sql",
		"spring.sql.init.data-locations=classpath*:db/postgres/data.sql" })
class AnalyticsComponentTest {

	@Autowired
	private AnalyticsComponent analyticsComponent;

	@Autowired
	private JdbcClient jdbcClient;

	@MockitoBean
	private AnalyticsAiService analyticsAiService;

	@BeforeEach
	void setup() {
		// Clean up tables before each test to ensure isolation
		jdbcClient.sql("DELETE FROM social_message").update();
	}

	@Test
	void testFullMessageLifecycle() {
		// 1. Prepare Data
		String msgId = UUID.randomUUID().toString();
		SocialMessage message = new SocialMessage(msgId, "TestNews",
				"This is a positive test message about AI technology.", "en", "Test User", "http://example.com",
				LocalDateTime.now());

		// 2. Insert Message
		analyticsComponent.insertSocialMessages(List.of(message));

		// Verify Insertion
		Integer count = jdbcClient.sql("SELECT count(*) FROM social_message WHERE id = ?")
			.param(msgId)
			.query(Integer.class)
			.single();
		assertThat(count).isEqualTo(1);

		// 3. Update TsVector (Search Vector)
		analyticsComponent.updateTsvector();

		// Verify TsVector entry created
		Integer tsCount = jdbcClient.sql("SELECT count(*) FROM message_entity_tsvector WHERE message_id = ?")
			.param(msgId)
			.query(Integer.class)
			.single();
		assertThat(tsCount).isEqualTo(1);

		// 4. Update Sentiment
		// Note: The postgres version of vaderSentimentFunction.py provided returns 0.0
		// dummy value
		analyticsComponent.updateVaderSentiment();

		// Verify Sentiment entry
		Double score = jdbcClient.sql("SELECT confidence_score FROM message_entity_sentiment WHERE message_id = ?")
			.param(msgId)
			.query(Double.class)
			.single();
		assertThat(score).isNotNull();

		// 5. Update Embeddings (Mocked AI)
		mockEmbeddingResponse();

		analyticsComponent.updateEmbeddings();

		// Verify Vector Store entry
		Integer vectorCount = jdbcClient.sql("SELECT count(*) FROM vector_store WHERE message_id = ?")
			.param(msgId)
			.query(Integer.class)
			.single();
		assertThat(vectorCount).isEqualTo(1);

		// 6. Delete Message
		analyticsComponent.deleteSocialMessages(List.of(msgId));

		// Verify Cascade Deletion
		Integer finalCount = jdbcClient.sql("SELECT count(*) FROM social_message WHERE id = ?")
			.param(msgId)
			.query(Integer.class)
			.single();
		assertThat(finalCount).isEqualTo(0);
	}

	@Test
	void testGisInfoUpdate() {
		// 1. Insert Message
		String msgId = UUID.randomUUID().toString();
		SocialMessage message = new SocialMessage(msgId, "GeoSource", "Event at Tokyo Tower", "en", "User", "url",
				LocalDateTime.now());
		analyticsComponent.insertSocialMessages(List.of(message));

		// 2. Mock AI GIS Response
		GisInfo mockGis = new GisInfo(msgId, message.createDateTime(), 4326, "POINT(139.7454 35.6586)",
				"Tokyo Tower location");
		when(analyticsAiService.getGisInfo(anyList())).thenReturn(List.of(mockGis));

		// 3. Execute Update
		analyticsComponent.updateGuessGisInfo();

		// 4. Verify Database
		String reason = jdbcClient.sql("SELECT reason FROM gis_info WHERE message_id = ?")
			.param(msgId)
			.query(String.class)
			.single();
		assertThat(reason).isEqualTo("Tokyo Tower location");
	}

	private void mockEmbeddingResponse() {
		// Mock the structure of the EmbeddingResponse from Spring AI
		float[] dummyVector = new float[1024]; // Assuming 1024 dimension
		Embedding embedding = new Embedding(dummyVector, 0);
		EmbeddingResponse response = new EmbeddingResponse(List.of(embedding));

		when(analyticsAiService.getEmbeddingResponse(anyList())).thenReturn(response);
	}

}

@TestConfiguration
class TestAsyncConfig {

	// this will overwrite the default executor
	@Bean
	public Executor taskExecutor() {
		return new SyncTaskExecutor();
	}

}