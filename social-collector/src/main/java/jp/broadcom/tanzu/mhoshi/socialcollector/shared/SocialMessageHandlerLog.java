package jp.broadcom.tanzu.mhoshi.socialcollector.shared;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;


@Component
@ConditionalOnProperty(value = "handler.debug", havingValue = "true")
class SocialMessageHandlerLog {

	@Bean
    Consumer<List<SocialMessage>> socialMessageConsumer() {
        return messages -> {
            messages.forEach(socialMessage -> {
                System.out.println(socialMessage);
            });
        };
    }
}
