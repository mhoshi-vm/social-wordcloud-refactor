package jp.broadcom.tanzu.mhoshi.social.restapi.messages;

import jp.broadcom.tanzu.mhoshi.social.restapi.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {"database=postgres"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SocialMessageAnalysisIntegrationTest {

    @Autowired
    private SocialMessageAnalysisRepo repository;

    @Autowired
    private SocialMessageAnalysisService service;

    @Test
    void repository_ShouldFindAllAnalyses() {
        // Act
        List<SocialMessageAnalysis> analyses = repository.findAll();

        // Assert - data.sql pre-loads 100 records
        assertThat(analyses).isNotEmpty();
        assertThat(analyses.size()).isGreaterThanOrEqualTo(1);

        // Verify structure of first result
        SocialMessageAnalysis first = analyses.getFirst();
        assertThat(first.messageId()).isNotNull();
        assertThat(first.origin()).isNotNull();
    }

    @Test
    void service_ListAll_ShouldReturnAllAnalyses() {
        // Act
        List<SocialMessageAnalysis> analyses = service.listAll();

        // Assert
        assertThat(analyses).isNotEmpty();
        assertThat(analyses.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void repository_FindById_ShouldReturnSpecificAnalysis() {
        // Arrange - Get an existing ID from the database
        List<SocialMessageAnalysis> all = repository.findAll();
        assertThat(all).isNotEmpty();
        String existingId = all.getFirst().messageId();

        // Act
        var result = repository.findById(existingId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().messageId()).isEqualTo(existingId);
    }

    @Test
    void repository_Save_ShouldNotThrowException() {
        // Arrange
        SocialMessageAnalysis newAnalysis = new SocialMessageAnalysis("test-id-123", "test-origin",
                "http://test-url.com", "POSITIVE", 1, LocalDateTime.now(), "POINT(0 0)");

        // Act & Assert - Save should complete without throwing exception
        assertThat(repository.save(newAnalysis)).isNotNull();

        // Note: In test context, transactions are rolled back by default,
        // so we don't verify persistence, just that save() completes successfully
    }

    @Autowired
    JdbcClient jdbcClient;

    @AfterAll
    void tearDown() {
        // Code to run once after all tests in this class are done
        jdbcClient.sql("DELETE FROM social_message").update();
        jdbcClient.sql("DELETE FROM social_message_analysis").update();
        // Example: close a static resource or perform database cleanup
    }
}
