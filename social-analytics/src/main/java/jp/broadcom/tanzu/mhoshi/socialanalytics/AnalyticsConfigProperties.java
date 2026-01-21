package jp.broadcom.tanzu.mhoshi.socialanalytics;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("analytics")
record AnalyticsConfigProperties(
        @DefaultValue("h2")
        String database,
        @DefaultValue("900000")
        Integer termFrequencyInterval,
        @DefaultValue("900000")
        Integer updateTsvectorInterval,
        @DefaultValue("900000")
        Integer updateVaderSentimentInterval,
        @DefaultValue("1800000")
        Integer updateEmbeddingsInterval,
        @DefaultValue("3600000")
        Integer updateGuessGisInfo,
        @DefaultValue("0 15 1 * * 6")
        String maintenanceCron
) {
}
