package jp.broadcom.tanzu.mhoshi.social.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

//If running on Apple Silicon, may need to run with
//DOCKER_HOST=tcp://<remote-host-ip-running-x86>:2375
//to support vector columns
@SpringBootTest
@Testcontainers
@Tag("integration")
@ContextConfiguration(classes = TestAsyncConfig.class)
@TestPropertySource(properties = { "database=greenplum", "analytics.database=greenplum", "spring.sql.init.mode=always",
		"spring.sql.init.schema-locations=classpath*:db/greenplum/schema.sql" })
@Import(GreenplumTestContainersConfiguration.class)
@Disabled
class GreenplumIntegrationTest {

	@Autowired
	private AnalyticsComponent analyticsComponent;

	@Autowired
	private JdbcClient jdbcClient;

	@MockitoBean
	private AnalyticsAiService analyticsAiService;

	@BeforeEach
	void setup() {
		// Clean state before every test to ensure isolation
		jdbcClient.sql("DELETE FROM social_message").update();
	}

	@Test
	void testSchemaTablesExist() {
		// Verify that the main tables and Materialized Views created by schema.sql exist
		// We check for specific Greenplum tables/views
		List<String> expectedTables = List.of("social_message", "hourly_message_stats", "daily_stock_metrics",
				"gis_info");

		for (String tableName : expectedTables) {
			Integer count = jdbcClient.sql("SELECT count(*) FROM pg_class WHERE relname = ?")
				.param(tableName)
				.query(Integer.class)
				.single();
			assertThat(count).as("Table or View '%s' should exist in the schema", tableName).isEqualTo(1);
		}
	}

	@Test
	void testAnalysisAndMaterializedViews() {
		// 1. Seed Data
		String msgId = seedStockMessage();

		// 2. Mock AI Responses
		mockAiServices(msgId);

		// 3. Run Analysis Updates
		analyticsComponent.updateTsvector();
		analyticsComponent.updateVaderSentiment();
		analyticsComponent.updateEmbeddings();
		analyticsComponent.updateGuessGisInfo();

		// 5. Assertions
		// Check Sentiment
		Double sentimentScore = jdbcClient
			.sql("SELECT confidence_score FROM message_entity_sentiment WHERE message_id = ?")
			.param(msgId)
			.query(Double.class)
			.single();
		assertThat(sentimentScore).as("Sentiment analysis should populate confidence score").isNotNull();

		// Check Stock Metrics MV
		Long stockCount = jdbcClient.sql("SELECT count(*) FROM daily_stock_metrics WHERE ticker = 'AVGO'")
			.query(Long.class)
			.single();
		assertThat(stockCount).as("Stock Metrics MV should aggregate the seeded AVGO data").isEqualTo(1);
	}

	@Test
	void testMaintenanceCreatesPartitions() {
		// 1. Execute Maintenance
		analyticsComponent.dbMaintenance();

		// 2. Calculate expected partition name for the NEXT month
		// Logic mirrors maintenance.sql: '_y' || to_char(next_month_start, 'YYYY_mMM')
		LocalDate nextMonth = LocalDate.now().plusMonths(1);
		String expectedSuffix = "_y" + DateTimeFormatter.ofPattern("yyyy_MM").format(nextMonth);

		// Note: Java's 'MM' is month number. The SQL pattern 'mMM' implies 'm' literal +
		// Month number.
		// Based on SQL: to_char(..., 'YYYY_mMM') -> 2026_m02
		String partitionNamePattern = "social_message_y" + DateTimeFormatter.ofPattern("yyyy_'m'MM").format(nextMonth);

		// 3. Verify Partition Exists in Catalog
		Boolean partitionExists = jdbcClient.sql("SELECT EXISTS (SELECT 1 FROM pg_class WHERE relname = ?)")
			.param(partitionNamePattern)
			.query(Boolean.class)
			.single();

		assertThat(partitionExists).as("Maintenance script should create partition table '%s'", partitionNamePattern)
			.isTrue();
	}

	@Test
	void testDeletionRemovesAllData() {
		// 1. Seed Data
		String msgId = seedStockMessage();

		// Ensure data is there initially
		Integer initialCount = jdbcClient.sql("SELECT count(*) FROM social_message WHERE id = ?")
			.param(msgId)
			.query(Integer.class)
			.single();
		assertThat(initialCount).isEqualTo(1);

		// 2. Mock services (needed if deletion triggers anything, though usually it's
		// just DB calls)
		// No strict mocks needed for pure deletion unless the component calls AI during
		// delete (unlikely).

		// 3. Execute Deletion
		analyticsComponent.deleteSocialMessages(List.of(msgId));

		// 4. Verify Cascade/Manual Deletion
		// The stored procedure 'delete_social_message_batch' handles cleanup of all
		// related tables
		List<String> tables = List.of("social_message", "message_entity_sentiment", "message_entity_tsvector",
				"vector_store", "gis_info");

		for (String table : tables) {
			// Note: child tables use 'message_id', parent uses 'id'.
			String idColumn = table.equals("social_message") ? "id" : "message_id";

			Integer count = jdbcClient.sql("SELECT count(*) FROM " + table + " WHERE " + idColumn + " = ?")
				.param(msgId)
				.query(Integer.class)
				.single();

			assertThat(count).as("Record in table '%s' should be deleted", table).isEqualTo(0);
		}
	}

	@Test
	void testGisCentroidsCalculation() {
		// 1. Seed 5 messages with different locations to meet the row_count >= 5
		// condition in maintenance.sql
		// Locations: Tokyo, Osaka, Nagoya, Fukuoka, Sapporo
		String[][] locations = { { "Tokyo", "POINT(139.6917 35.6895)" }, { "Osaka", "POINT(135.5023 34.6937)" },
				{ "Nagoya", "POINT(136.9066 35.1815)" }, { "Fukuoka", "POINT(130.4017 33.5904)" },
				{ "Sapporo", "POINT(141.3545 43.0621)" } };

		for (int i = 0; i < locations.length; i++) {
			String msgId = UUID.randomUUID().toString();
			LocalDateTime now = LocalDateTime.now();

			// Insert parent message
			SocialMessage message = new SocialMessage(msgId, "GeoTest", "Location: " + locations[i][0], "en", "User",
					"url", now);
			analyticsComponent.insertSocialMessages(List.of(message));

			// Insert GIS info manually to simulate AI discovery
			jdbcClient
				.sql("INSERT INTO gis_info (message_id, msg_timestamp, srid, gis, reason) VALUES (?, ?, 4326, ?, ?)")
				.params(msgId, now, locations[i][1], "Manual seed for " + locations[i][0])
				.update();
		}

		// 2. Execute Maintenance - This calls train_and_refresh_clusters() inside the SQL
		// block
		// based on the condition 'IF row_count >= 5' in maintenance.sql
		analyticsComponent.dbMaintenance();

		// 3. Verify centroids were defined in the results table
		Integer centroidCount = jdbcClient.sql("SELECT array_upper((kmeanspp).centroids, 1) FROM gis_kmeans_result")
			.query(Integer.class)
			.single();
		assertThat(centroidCount).as("Madlib should have generated 5 cluster centroids").isEqualTo(5);

		// 4. Verify the Materialized View shows results
		// This view joins gis_info with the defined centers
		List<String> results = jdbcClient.sql("SELECT message_id FROM gis_info_w_centroids").query(String.class).list();

		assertThat(results).as("Materialized view should be refreshed and contain data").hasSizeGreaterThanOrEqualTo(5);
	}

	@Test
	void testDailyStockMetricsGapFilling() {
		// 1. Seed data with a gap (Jan 1st and Jan 3rd, skipping Jan 2nd)
		String ticker = "AVGO";
		LocalDateTime day1 = LocalDateTime.of(2026, 1, 1, 10, 0);
		LocalDateTime day3 = LocalDateTime.of(2026, 1, 3, 10, 0);
		LocalDate day2 = LocalDate.of(2026, 1, 2);

		SocialMessage msgDay1 = new SocialMessage(UUID.randomUUID().toString(), "stocksprice",
				"{\"ticker\":\"" + ticker + "\",\"price\":350.00,\"volume\":1000}", "en", "Bot", "url", day1);
		SocialMessage msgDay3 = new SocialMessage(UUID.randomUUID().toString(), "stocksprice",
				"{\"ticker\":\"" + ticker + "\",\"price\":360.00,\"volume\":2000}", "en", "Bot", "url", day3);

		analyticsComponent.insertSocialMessages(List.of(msgDay1, msgDay3));

		// 3. Verify Jan 2nd (the gap) exists and has Jan 1st's values (LOCF)
		// We query the specific bucket for the missing day
		var gapResult = jdbcClient.sql("""
				    SELECT avg_price, total_volume
				    FROM daily_stock_metrics
				    WHERE ticker = ? AND bucket = ?
				""")
			.params(ticker, Timestamp.valueOf(day2.atStartOfDay()))
			.query((rs, rowNum) -> new Object[] { rs.getDouble("avg_price"), rs.getLong("total_volume") })
			.single();

		Double filledPrice = (Double) gapResult[0];
		Long filledVolume = (Long) gapResult[1];

		assertThat(filledPrice).as("Gap day should carry forward price from Jan 1st").isEqualTo(350.00);
		assertThat(filledVolume).as("Gap day should carry forward volume from Jan 1st").isEqualTo(1000L);

		// 4. Verify Jan 3rd has its own actual values
		Double actualPriceDay3 = jdbcClient.sql("SELECT avg_price FROM daily_stock_metrics WHERE bucket = ?")
			.param(Timestamp.valueOf(day3.truncatedTo(java.time.temporal.ChronoUnit.DAYS)))
			.query(Double.class)
			.single();
		assertThat(actualPriceDay3).isEqualTo(360.00);
	}

	@Test
	void testSocialMessageAnalysisViewWithComponentLogic() {
		// 1. Seed 5 messages with different locations to satisfy MADlib clustering (k=5)
		// Only these 5 will have GIS info; others without GIS will be filtered by the
		// INNER JOIN
		String[][] locations = { { "Tokyo Tower", "POINT(139.7454 35.6586)" },
				{ "Osaka Castle", "POINT(135.5262 34.6873)" }, { "Nagoya TV Tower", "POINT(136.9084 35.1715)" },
				{ "Fukuoka Tower", "POINT(130.3515 33.5932)" }, { "Sapporo Clock Tower", "POINT(141.3533 43.0625)" } };

		LocalDateTime now = LocalDateTime.now();
		List<String> validIds = new java.util.ArrayList<>();

		for (String[] loc : locations) {
			String msgId = UUID.randomUUID().toString();
			validIds.add(msgId);
			SocialMessage msg = new SocialMessage(msgId, "GeoSource", "Event happening at " + loc[0], "en", "User",
					"http://example.com/" + msgId, now);
			analyticsComponent.insertSocialMessages(List.of(msg));
		}

		// Seed a 6th message WITHOUT GIS info to test the INNER JOIN filtering
		String msgIdNoGis = UUID.randomUUID().toString();
		analyticsComponent.insertSocialMessages(List
			.of(new SocialMessage(msgIdNoGis, "NoGeo", "Just text here.", "en", "User", "url", now.minusHours(1))));

		// 2. Mock AI Responses for the 5 valid messages
		List<GisInfo> mockGisResults = new java.util.ArrayList<>();
		for (int i = 0; i < locations.length; i++) {
			mockGisResults.add(new GisInfo(validIds.get(i), now, 4326, locations[i][1],
					"Found location in text: " + locations[i][0]));
		}

		// Ensure the AI service returns our 5 mock locations
		when(analyticsAiService.getGisInfo(anyList())).thenReturn(mockGisResults);

		// 3. Trigger Component Analysis Logic
		analyticsComponent.updateTsvector();
		analyticsComponent.updateVaderSentiment();
		analyticsComponent.updateGuessGisInfo();

		// 4. Refresh Materialized Views and train clusters via Maintenance
		// This executes the MADlib kmeanspp logic because row_count >= 5
		analyticsComponent.dbMaintenance();

		// 5. Query the analysis view for the processed records
		List<Map<String, Object>> viewResults = jdbcClient.sql("SELECT * FROM social_message_analysis")
			.query()
			.listOfRows();

		// 6. Assertions
		// The view should contain exactly 5 records (the one without GIS is filtered out)
		assertThat(viewResults).hasSize(5);

		for (Map<String, Object> row : viewResults) {
			assertThat(row.get("sentiment_label")).isNotNull(); // From
																// updateVaderSentiment
			assertThat(row.get("centroid_cluster_id")).isNotNull(); // From MADlib
																	// clusters
			assertThat(row.get("geom")).isNotNull(); // Verified GIS data exists
		}

		// Verify the message without GIS is indeed missing from the view
		Boolean existsNoGis = jdbcClient
			.sql("SELECT EXISTS(SELECT 1 FROM social_message_analysis WHERE message_id = ?)")
			.param(msgIdNoGis)
			.query(Boolean.class)
			.single();
		assertThat(existsNoGis).isFalse();
	}

	// --- Helpers ---

	private String seedStockMessage() {
		String msgId = UUID.randomUUID().toString();
		SocialMessage message = new SocialMessage(msgId, "stocksprice",
				"{\"ticker\":\"AVGO\",\"price\":150.00,\"volume\":1000}", "en", "TestBot", "http://example.com",
				LocalDateTime.now());
		analyticsComponent.insertSocialMessages(List.of(message));
		return msgId;
	}

	private void mockAiServices(String msgId) {
		// Mock Embeddings
		EmbeddingResponse mockEmbedding = new EmbeddingResponse(List.of(new Embedding(new float[1024], 0)));
		when(analyticsAiService.getEmbeddingResponse(anyList())).thenReturn(mockEmbedding);

		// Mock GIS
		GisInfo mockGis = new GisInfo(msgId, LocalDateTime.now(), 4326, "POINT(0 0)", "Test Reason");
		when(analyticsAiService.getGisInfo(anyList())).thenReturn(List.of(mockGis));
	}

}