package jp.broadcom.tanzu.mhoshi.socialanalytics;

import jakarta.annotation.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
class AnalyticsAiService {
    final static String GET_GIS_PROMPT = "ai/prompts/getGisInfo.txt";
    ChatClient chatClient;
    EmbeddingModel embeddingModel;

    AnalyticsAiService(ChatClient.Builder chatClientBuilder, EmbeddingModel embeddingModel) {
        this.chatClient = chatClientBuilder.build();
        this.embeddingModel = embeddingModel;
    }

    EmbeddingResponse getEmbeddingResponse(List<String> messages) {
        return this.embeddingModel.embedForResponse(messages);
    }

    @Nullable
    List<GisInfo> getGisInfo(List<String> messages) {
        List<GisInfo> gisInfos = new ArrayList<>();
        messages.forEach(message -> {
            GisInfo gisInfo = chatClient.prompt().user(u -> u.text(FileLoader.loadAsString(GET_GIS_PROMPT)).param("message", message))
                    .call()
                    .entity(GisInfo.class);
            if (gisInfo != null) {
                gisInfos.add(gisInfo);
            }
            System.out.println();
        });
        return gisInfos;
    }
}
