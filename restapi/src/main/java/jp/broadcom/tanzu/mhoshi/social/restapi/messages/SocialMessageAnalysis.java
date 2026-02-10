package jp.broadcom.tanzu.mhoshi.social.restapi.messages;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

record SocialMessageAnalysis(
// @formatter:off
		@Id
		String messageId,
		String origin,
		String url,
		String sentimentLabel,
		Integer centroidClusterId,
		LocalDateTime createDateTime,
		String geom
		// @formatter:on
) {
}
