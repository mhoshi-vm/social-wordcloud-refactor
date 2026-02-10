package jp.broadcom.tanzu.mhoshi.social.collector.mastodon;

import java.time.LocalDateTime;

record MastodonTimelinesResponse(
// @formatter:off
		String id,
		String content,
		String language,
		String url,
		LocalDateTime created_at,
		MastodonTimelinesResponseAccount account
		// @formatter:on
) {
	record MastodonTimelinesResponseAccount(
	// @formatter:off
			String name,
			String display_name
			// @formatter:on
	) {
	}
}
