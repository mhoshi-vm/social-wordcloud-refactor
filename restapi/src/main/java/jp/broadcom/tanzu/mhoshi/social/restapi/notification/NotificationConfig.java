package jp.broadcom.tanzu.mhoshi.social.restapi.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Configuration
class NotificationConfig {

	@Bean
	Consumer<List<NotificationMessage>> notificationConsumer(NotificationService notificationService) {
		return (in) -> {
			if (!in.isEmpty()) {
				for (SseEmitter emitter : notificationService.getEmitters()) {
					try {
						emitter.send(SseEmitter.event()
							.name("new entry")
							.data(String.format("New Entry from %s posted by %s ", in.getLast().origin(),
									in.getLast().name())));
					}
					catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		};
	}

}
