package jp.broadcom.tanzu.mhoshi.social.collector.apifylinkedin;

record ApifyLinkedInRequest(
// @formatter:off
		String keyword,
		Integer limit,
		String sort_type
		// @formatter:on
) {
}
