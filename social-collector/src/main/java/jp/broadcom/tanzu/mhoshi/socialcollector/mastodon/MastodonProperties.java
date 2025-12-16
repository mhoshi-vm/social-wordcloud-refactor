package jp.broadcom.tanzu.mhoshi.socialcollector.mastodon;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("mastodon")
record MastodonProperties(
        @DefaultValue("https")
        String scheme,
        @DefaultValue("mstdn.social")
        String url,
        @DefaultValue("443")
        Integer port,
        String token,
        @DefaultValue("40")
        Integer pollingLimit,
        @DefaultValue("Broadcom")
        String hashTag
) {
}
