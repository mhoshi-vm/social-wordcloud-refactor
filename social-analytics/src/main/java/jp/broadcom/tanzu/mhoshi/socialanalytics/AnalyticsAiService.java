package jp.broadcom.tanzu.mhoshi.socialanalytics;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class AnalyticsAiService {
    ChatClient chatClient;
    EmbeddingModel embeddingModel;


    final static String GET_GIS_PROMPT = "ai/prompts/getGisInfo.txt";

    AnalyticsAiService(ChatClient.Builder chatClientBuilder, EmbeddingModel embeddingModel) {
        this.chatClient = chatClientBuilder.build();
        this.embeddingModel = embeddingModel;
    }

    EmbeddingResponse getEmbeddingResponse(List<String> messages) {


        return this.embeddingModel.embedForResponse(messages);
    }



    List<GisInfo> getGisInfo(List<String> messages) {
        return chatClient.prompt().user(u -> u.text(FileLoader.loadAsString(GET_GIS_PROMPT)).param("messages", messages))
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
    }




}
