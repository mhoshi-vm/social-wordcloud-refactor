package jp.broadcom.tanzu.mhoshi.social.analytics;

import java.time.LocalDateTime;

record SocialMessage(String id, String origin, String text, String lang, String name, String url,
		LocalDateTime createDateTime) {
}
