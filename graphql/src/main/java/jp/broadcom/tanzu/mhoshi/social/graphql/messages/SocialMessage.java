package jp.broadcom.tanzu.mhoshi.social.graphql.messages;

import jakarta.annotation.Nullable;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

record SocialMessage(
// @formatter:off
		@Id
        @Nullable
		String id,
		String origin,
		@Nullable
        String text,
		String lang,
		String name,
		@Nullable
        String url,
		@Nullable
        LocalDateTime createDateTime
		// @formatter:on
) {
}
