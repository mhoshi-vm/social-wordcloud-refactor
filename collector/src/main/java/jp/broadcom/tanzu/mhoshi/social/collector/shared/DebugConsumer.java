package jp.broadcom.tanzu.mhoshi.social.collector.shared;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Consumer;

@Configuration
class DebugConsumer {

    @Bean
    Consumer<List<SocialMessage>> printMessages() {
        return message -> message.forEach(System.out::println);
    }
}
