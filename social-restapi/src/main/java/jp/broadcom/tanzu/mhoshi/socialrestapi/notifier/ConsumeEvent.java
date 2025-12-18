package jp.broadcom.tanzu.mhoshi.socialrestapi.notifier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Consumer;

@Configuration
class ConsumeEvent {

    @Bean
    Consumer<List<String>> notifyEvents(NotificationEventService notificationEventService) {
        return message -> message.forEach(messageEntity -> {
                        notificationEventService.notify(messageEntity);
                }
        );

    }
}
