package jp.broadcom.tanzu.mhoshi.socialanalytics;

import io.pivotal.cfenv.boot.genai.GenaiLocator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConditionalOnExpression("'${spring.profiles.active}'.startsWith('cf')")
class CfGenAiModelLocator {

    @Bean
    EmbeddingModel embeddingModel(GenaiLocator locator) {
        return locator.getFirstAvailableEmbeddingModel();
    }

    @Bean
    ChatModel chatModel(GenaiLocator locator) {
        return locator.getFirstAvailableChatModel();
    }
}
