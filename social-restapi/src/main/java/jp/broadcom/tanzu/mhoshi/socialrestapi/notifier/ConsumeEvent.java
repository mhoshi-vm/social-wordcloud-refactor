package jp.broadcom.tanzu.mhoshi.socialrestapi.notifier;

import jp.broadcom.tanzu.mhoshi.socialrestapi.message.EventAction;
import jp.broadcom.tanzu.mhoshi.socialrestapi.message.MessageEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Consumer;

@Configuration
class ConsumeEvent {

    @Bean
    Consumer<List<MessageEntity>> notifyEvents(NotificationEventService notificationEventService) {
        return message -> message.forEach(messageEntity -> {
                    if (messageEntity.getAction().equals(EventAction.INSERT))
                        notificationEventService.notify(messageEntity);
                }
        );

    }
}
