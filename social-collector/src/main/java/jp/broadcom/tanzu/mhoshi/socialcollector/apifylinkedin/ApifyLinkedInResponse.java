package jp.broadcom.tanzu.mhoshi.socialcollector.apifylinkedin;

import java.time.Instant;

record ApifyLinkedInResponse(
        String activity_id,
        String post_url,
        String text,
        ApifyLinkedInResponseAuthor author,
        ApifyLinkedInResponsePostedAt posted_at) {
    record ApifyLinkedInResponseAuthor(String name) {
    }

    record ApifyLinkedInResponsePostedAt(
            Long timestamp) {
    }
}
