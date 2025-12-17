package jp.broadcom.tanzu.mhoshi.socialcollector.apifylinkedin;

record ApifyLinkedInRequest(
        String keyword,
        Integer limit,
        String sort_type
) {
}
