package jp.broadcom.tanzu.mhoshi.social.analytics;

import java.time.LocalDateTime;

record GisInfo(
// @formatter:off
		String messageId,
		LocalDateTime createDateTime,
		Integer srid,
		String gis,
		String reason
		// @formatter:on
) {
}
