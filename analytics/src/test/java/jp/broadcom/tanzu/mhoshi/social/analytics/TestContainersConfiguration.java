package jp.broadcom.tanzu.mhoshi.social.analytics;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@TestConfiguration(proxyBeanMethods = false)
class TestContainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(DockerImageName.parse("timescale/timescaledb-ha:pg15.15-ts2.24.0-all")
			.asCompatibleSubstituteFor("postgres")).withEnv("POSTGRES_HOST_AUTH_METHOD", "trust")

			.waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\s", 2))
			.withStartupCheckStrategy(new MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(5)));

	}

}