package jp.broadcom.tanzu.mhoshi.social.collector.apifylinkedin;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("apifylinkedin")
record ApifyLinkedInProperties(@DefaultValue("https") String scheme, @DefaultValue("api.apify.com") String url,
		@DefaultValue("apimaestro~linkedin-posts-search-scraper-no-cookies") String appId, String token,
		@DefaultValue("3") Integer pollingLimit, @DefaultValue("date_posted") String sortType,
		@DefaultValue("Broadcom") String keyword) {
}
