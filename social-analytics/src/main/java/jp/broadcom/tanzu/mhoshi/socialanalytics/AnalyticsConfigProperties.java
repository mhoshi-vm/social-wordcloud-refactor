package jp.broadcom.tanzu.mhoshi.socialanalytics;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("analytics")
record AnalyticsConfigProperties(
        @DefaultValue("h2")
        String database,
        @DefaultValue("15000")
        Integer termFrequencyInterval,
        @DefaultValue("15000")
        Integer updateTsvectorInterval,
        @DefaultValue("15000")
        Integer updateVaderSentimentInterval,
        @DefaultValue("60000")
        Integer updateEmbeddingsInterval
) {
}
