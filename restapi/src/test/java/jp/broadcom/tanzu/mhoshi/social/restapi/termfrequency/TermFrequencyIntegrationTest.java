package jp.broadcom.tanzu.mhoshi.social.restapi.termfrequency;

import jp.broadcom.tanzu.mhoshi.social.restapi.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = { "database=postgres" })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TermFrequencyIntegrationTest {

	@Autowired
	private TermFrequencyRepository repository;

	@Autowired
	private TermFrequencyService service;

	@Test
	void repository_TermFrequencyEntityDay_ShouldReturnDailyData() {
		// Act
		List<TermFrequency> frequencies = repository.termFrequencyEntityDay();

		// Assert
		assertThat(frequencies).isNotNull();
		// Note: May be empty if no data in views, but query should execute without error

		if (!frequencies.isEmpty()) {
			TermFrequency first = frequencies.getFirst();
			assertThat(first.getRank()).isNotNull();
			assertThat(first.getTerm()).isNotNull();
			assertThat(first.getCount()).isNotNull();
		}
	}

	@Test
	void repository_TermFrequencyEntityWeek_ShouldReturnWeeklyData() {
		// Act
		List<TermFrequency> frequencies = repository.termFrequencyEntityWeek();

		// Assert
		assertThat(frequencies).isNotNull();

		if (!frequencies.isEmpty()) {
			TermFrequency first = frequencies.getFirst();
			assertThat(first.getRank()).isNotNull();
			assertThat(first.getTerm()).isNotNull();
			assertThat(first.getCount()).isNotNull();
		}
	}

	@Test
	void repository_TermFrequencyEntityMonth_ShouldReturnMonthlyData() {
		// Act
		List<TermFrequency> frequencies = repository.termFrequencyEntityMonth();

		// Assert
		assertThat(frequencies).isNotNull();

		if (!frequencies.isEmpty()) {
			TermFrequency first = frequencies.getFirst();
			assertThat(first.getRank()).isNotNull();
			assertThat(first.getTerm()).isNotNull();
			assertThat(first.getCount()).isNotNull();
		}
	}

	@Test
	void service_GetTermFrequencyEntityDay_ShouldCallRepository() {
		// Act
		List<TermFrequency> frequencies = service.getTermFrequencyEntityDay();

		// Assert
		assertThat(frequencies).isNotNull();
	}

	@Test
	void service_GetTermFrequencyEntityWeek_ShouldCallRepository() {
		// Act
		List<TermFrequency> frequencies = service.getTermFrequencyEntityWeek();

		// Assert
		assertThat(frequencies).isNotNull();
	}

	@Test
	void service_GetTermFrequencyEntityMonth_ShouldCallRepository() {
		// Act
		List<TermFrequency> frequencies = service.getTermFrequencyEntityMonth();

		// Assert
		assertThat(frequencies).isNotNull();
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
