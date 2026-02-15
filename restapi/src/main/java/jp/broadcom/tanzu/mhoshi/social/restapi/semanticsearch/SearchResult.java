package jp.broadcom.tanzu.mhoshi.social.restapi.semanticsearch;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

record SearchResult(
// @formatter:off
        @Id String id,
        String messageId,
        LocalDateTime msgTimestamp,
        Float distance
// @formatter:on

) {
}
