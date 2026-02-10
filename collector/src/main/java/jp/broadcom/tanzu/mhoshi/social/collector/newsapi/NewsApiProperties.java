package jp.broadcom.tanzu.mhoshi.social.collector.newsapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("newsapi")
record NewsApiProperties(
// @formatter:off
		String key,
		@DefaultValue("newsapi.org")
		String url,
		@DefaultValue("https")
		String scheme,
		@DefaultValue("100")
		Integer limit,
		@DefaultValue("\"Broadcom\"")
		String query,
		@DefaultValue("biztoc.com")
		String excludeDomains,
		@DefaultValue("en")
		String language
		// @formatter:on
) {
}
