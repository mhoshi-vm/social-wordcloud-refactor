package jp.broadcom.tanzu.mhoshi.socialcollector.newsapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("newsapi")
record NewsApiProperties(
        String key,
        @DefaultValue("newsapi.org") String url,
        @DefaultValue("https") String scheme,
        @DefaultValue("100") Integer limit,
        @DefaultValue("\"Broadcom\"") String query,
        @DefaultValue("biztoc.com") String excludeDomains,
        @DefaultValue("en") String language
) {
}
