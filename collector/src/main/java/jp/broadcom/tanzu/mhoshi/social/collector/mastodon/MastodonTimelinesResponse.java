package jp.broadcom.tanzu.mhoshi.social.collector.mastodon;

import java.time.LocalDateTime;

record MastodonTimelinesResponse(
        String id,
        String content,
        String language,
        String url,
        LocalDateTime created_at,
        MastodonTimelinesResponseAccount account) {
    record MastodonTimelinesResponseAccount(String name, String display_name) {
    }
}
