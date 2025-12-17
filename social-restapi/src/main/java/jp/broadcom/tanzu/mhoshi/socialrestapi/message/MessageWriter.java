package jp.broadcom.tanzu.mhoshi.socialrestapi.message;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Consumer;

@Configuration
class MessageWriter {

    @Bean
    Consumer<List<MessageEntity>> messageConsumer(MessageService messageService) {
        return (in)->{
            messageService.saveAll(in);
        };
    }

}
