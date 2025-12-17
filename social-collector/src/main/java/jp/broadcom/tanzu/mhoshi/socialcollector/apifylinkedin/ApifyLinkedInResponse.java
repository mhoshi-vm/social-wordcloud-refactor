package jp.broadcom.tanzu.mhoshi.socialcollector.apifylinkedin;

import com.fasterxml.jackson.annotation.JsonFormat;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.Instant;
import java.time.LocalDateTime;

record ApifyLinkedInResponse(
        String activity_id,
        String post_url,
        String text,
        ApifyLinkedInResponseAuthor author,
        ApifyLinkedInResponsePostedAt posted_at) {
    record ApifyLinkedInResponseAuthor(String name) {
    }

    record ApifyLinkedInResponsePostedAt(
            Instant timestamp) {
    }
}
