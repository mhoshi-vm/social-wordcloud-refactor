package jp.broadcom.tanzu.mhoshi.socialanalytics;

import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
@TestPropertySource(properties = {
        "database=greenplum",
        "analytics.database=greenplum",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath*:db/greenplum/schema.sql"
})
@Import(GreenplumTestContainersConfiguration.class)
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
        List<String> expectedTables = List.of("social_message", "hourly_message_stats", "daily_stock_metrics", "gis_info");

        for (String tableName : expectedTables) {
            Integer count = jdbcClient.sql("SELECT count(*) FROM pg_class WHERE relname = ?")
                    .param(tableName)
                    .query(Integer.class)
                    .single();
            assertThat(count)
                    .as("Table or View '%s' should exist in the schema", tableName)
                    .isEqualTo(1);
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
        Double sentimentScore = jdbcClient.sql("SELECT confidence_score FROM message_entity_sentiment WHERE message_id = ?")
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

        // Note: Java's 'MM' is month number. The SQL pattern 'mMM' implies 'm' literal + Month number.
        // Based on SQL: to_char(..., 'YYYY_mMM') -> 2026_m02
        String partitionNamePattern = "social_message_y" + DateTimeFormatter.ofPattern("yyyy_'m'MM").format(nextMonth);

        // 3. Verify Partition Exists in Catalog
        Boolean partitionExists = jdbcClient.sql("SELECT EXISTS (SELECT 1 FROM pg_class WHERE relname = ?)")
                .param(partitionNamePattern)
                .query(Boolean.class)
                .single();

        assertThat(partitionExists)
                .as("Maintenance script should create partition table '%s'", partitionNamePattern)
                .isTrue();
    }

    @Test
    void testDeletionRemovesAllData() {
        // 1. Seed Data
        String msgId = seedStockMessage();

        // Ensure data is there initially
        Integer initialCount = jdbcClient.sql("SELECT count(*) FROM social_message WHERE id = ?").param(msgId).query(Integer.class).single();
        assertThat(initialCount).isEqualTo(1);

        // 2. Mock services (needed if deletion triggers anything, though usually it's just DB calls)
        // No strict mocks needed for pure deletion unless the component calls AI during delete (unlikely).

        // 3. Execute Deletion
        analyticsComponent.deleteSocialMessages(List.of(msgId));

        // 4. Verify Cascade/Manual Deletion
        // The stored procedure 'delete_social_message_batch' handles cleanup of all related tables
        List<String> tables = List.of(
                "social_message",
                "message_entity_sentiment",
                "message_entity_tsvector",
                "vector_store",
                "gis_info"
        );

        for (String table : tables) {
            // Note: child tables use 'message_id', parent uses 'id'.
            String idColumn = table.equals("social_message") ? "id" : "message_id";

            Integer count = jdbcClient.sql("SELECT count(*) FROM " + table + " WHERE " + idColumn + " = ?")
                    .param(msgId)
                    .query(Integer.class)
                    .single();

            assertThat(count)
                    .as("Record in table '%s' should be deleted", table)
                    .isEqualTo(0);
        }
    }

    // --- Helpers ---

    private String seedStockMessage() {
        String msgId = UUID.randomUUID().toString();
        SocialMessage message = new SocialMessage(
                msgId,
                "stocksprice",
                "{\"ticker\":\"AVGO\",\"price\":150.00,\"volume\":1000}",
                "en",
                "TestBot",
                "http://example.com",
                LocalDateTime.now()
        );
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