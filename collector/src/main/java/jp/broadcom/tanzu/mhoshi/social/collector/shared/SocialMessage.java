package jp.broadcom.tanzu.mhoshi.social.collector.shared;

import java.time.LocalDateTime;

public record SocialMessage(
// @formatter:off
		String id,
		String origin,
		String text,
		String lang,
		String name,
		String url,
		LocalDateTime createDateTime
		// @formatter:on
) {
}
