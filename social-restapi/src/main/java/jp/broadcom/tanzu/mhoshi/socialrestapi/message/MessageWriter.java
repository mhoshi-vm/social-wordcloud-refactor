package jp.broadcom.tanzu.mhoshi.socialrestapi.message;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Configuration
class MessageWriter {

    @Bean
    Consumer<List<MessageEntity>> messageConsumer(MessageService messageService) {
        return (in)->{
            Path file = Paths.get("output.txt");
            List<String> lines = new ArrayList<>();
            in.forEach(
                    messageEntity -> lines.add(messageEntity.toString())
            );
            try {
                Files.write(file, lines);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            messageService.saveAll(in);
        };
    }

}
