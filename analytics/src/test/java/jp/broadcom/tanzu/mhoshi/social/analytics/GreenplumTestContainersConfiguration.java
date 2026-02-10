package jp.broadcom.tanzu.mhoshi.social.analytics;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class GreenplumTestContainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(DockerImageName.parse("gp7-analytics").asCompatibleSubstituteFor("postgres"))
			.waitingFor(Wait.forLogMessage("^.*statement: GRANT ALL PRIVILEGES ON DATABASE.*\\n", 1))
			.withReuse(true);

	}

}