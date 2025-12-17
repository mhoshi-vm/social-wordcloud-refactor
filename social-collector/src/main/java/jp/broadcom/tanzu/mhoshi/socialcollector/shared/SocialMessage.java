package jp.broadcom.tanzu.mhoshi.socialcollector.shared;

import jakarta.annotation.Nullable;

import java.time.LocalDateTime;

public record SocialMessage(String id, String origin, String text, String lang, String name, String url,
                            LocalDateTime createDateTime, EventAction action) {
}
