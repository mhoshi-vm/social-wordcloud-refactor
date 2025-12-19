package jp.broadcom.tanzu.mhoshi.socialcollector.stocksapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("stocksapi")
record StocksApiProperties(
        @DefaultValue("https")
        String scheme,
        @DefaultValue("api.api-ninjas.com")
        String url,
        String apiKey,
        @DefaultValue("AVGO")
        String ticker
) {
}
