package jp.broadcom.tanzu.mhoshi.social.analytics;

import java.time.LocalDateTime;

record GisInfo(
        String messageId,
        LocalDateTime createDateTime,
        Integer srid,
        String gis,
        String reason
) {
}
