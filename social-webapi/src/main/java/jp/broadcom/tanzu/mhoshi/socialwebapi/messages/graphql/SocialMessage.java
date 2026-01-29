package jp.broadcom.tanzu.mhoshi.socialwebapi.messages.graphql;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;


record SocialMessage(@Id String id, String origin, String text, String lang, String name, String url,
                     LocalDateTime createDateTime) {
}
