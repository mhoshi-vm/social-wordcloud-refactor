package jp.broadcom.tanzu.mhoshi.social.restapi.messages;

import jp.broadcom.tanzu.mhoshi.social.restapi.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
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
	void repository_Save_ShouldPersistNewAnalysis() {
		// Arrange
		SocialMessageAnalysis newAnalysis = new SocialMessageAnalysis("test-id-123", "test-origin",
				"http://test-url.com", "POSITIVE", 1, LocalDateTime.now(), "POINT(0 0)");

		// Act
		repository.save(newAnalysis);

		// Assert
		var saved = repository.findById("test-id-123");
		assertThat(saved).isPresent();
		assertThat(saved.get().origin()).isEqualTo("test-origin");
		assertThat(saved.get().sentimentLabel()).isEqualTo("POSITIVE");

		// Cleanup
		repository.deleteById("test-id-123");
	}

}
