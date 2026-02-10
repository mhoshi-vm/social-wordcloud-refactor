package jp.broadcom.tanzu.mhoshi.social.restapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = { "database=postgres" })
class RestapiApplicationTests {

	@Test
	void contextLoads() {
	}

}
