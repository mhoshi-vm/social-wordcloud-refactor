package jp.broadcom.tanzu.mhoshi.socialanalytics;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("analytics")
record AnalyticsConfigProperties(
        @DefaultValue("false")
        Boolean postgresEnabled,
        @DefaultValue("15000")
        Integer termFrequencyInterval,
        @DefaultValue("15000")
        Integer updateTsvectorInterval,
        @DefaultValue("15000")
        Integer updateVaderSentimentInterval
) {
}
