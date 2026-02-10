package jp.broadcom.tanzu.mhoshi.social.collector.apifylinkedin;

import java.time.Instant;

record ApifyLinkedInResponse(
// @formatter:off
		String activity_id,
		String post_url,
		String text,
		ApifyLinkedInResponseAuthor author,
		ApifyLinkedInResponsePostedAt posted_at
		// @formatter:on
) {
	record ApifyLinkedInResponseAuthor(
	// @formatter:off
			String name
			// @formatter:on
	) {
	}

	record ApifyLinkedInResponsePostedAt(
	// @formatter:off
			Long timestamp
			// @formatter:on
	) {
	}
}
