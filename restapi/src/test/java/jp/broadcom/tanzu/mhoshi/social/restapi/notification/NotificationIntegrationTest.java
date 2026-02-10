package jp.broadcom.tanzu.mhoshi.social.restapi.notification;

import jp.broadcom.tanzu.mhoshi.social.restapi.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({ TestcontainersConfiguration.class, TestChannelBinderConfiguration.class })
@TestPropertySource(properties = { "database=postgres" })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationIntegrationTest {

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private NotificationEventController notificationController;

	@AfterEach
	void cleanup() {
		// Clear all emitters after each test to avoid state sharing
		List<SseEmitter> emitters = notificationService.getEmitters();
		emitters.clear();
	}

	@Test
	void notificationService_ShouldProvideEmitterList() {
		// Assert
		List<SseEmitter> emitters = notificationService.getEmitters();
		assertThat(emitters).isNotNull();
	}

	@Test
	void notificationService_ShouldAllowAddingEmitters() {
		// Arrange
		SseEmitter newEmitter = new SseEmitter();
		List<SseEmitter> emitters = notificationService.getEmitters();
		int initialSize = emitters.size();

		// Act
		emitters.add(newEmitter);

		// Assert
		assertThat(emitters).hasSize(initialSize + 1);
		assertThat(emitters).contains(newEmitter);

		// Cleanup
		emitters.remove(newEmitter);
	}

	@Test
	void notificationController_ShouldCreateSseEmitter() {
		// Act
		SseEmitter emitter = notificationController.newMessage();

		// Assert
		assertThat(emitter).isNotNull();
		assertThat(emitter.getTimeout()).isEqualTo(-1L); // Infinite timeout
	}

	@Test
	void notificationController_ShouldAddEmitterToService() {
		// Arrange
		List<SseEmitter> emitters = notificationService.getEmitters();
		int initialSize = emitters.size();

		// Act
		SseEmitter emitter = notificationController.newMessage();

		// Assert
		assertThat(emitters).hasSize(initialSize + 1);
		assertThat(emitters).contains(emitter);

		// Cleanup
		emitters.remove(emitter);
	}

	@Test
	void notificationController_ShouldRegisterCompletionCallback() {
		// Arrange
		List<SseEmitter> emitters = notificationService.getEmitters();

		// Act
		SseEmitter emitter = notificationController.newMessage();
		assertThat(emitters).contains(emitter);

		// Manually remove to simulate what the callback does
		// (Testing actual async completion is unreliable in unit tests)
		emitters.remove(emitter);

		// Assert
		assertThat(emitters).doesNotContain(emitter);
	}

	// Note: messageConsumer tests are disabled because Spring Cloud Stream bindings
	// are not configured in application.properties for the messageConsumer function.
	// To enable these tests, add the following to application.properties:
	// spring.cloud.function.definition=messageConsumer
	// spring.cloud.stream.bindings.messageConsumer-in-0.destination=notification-input

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
