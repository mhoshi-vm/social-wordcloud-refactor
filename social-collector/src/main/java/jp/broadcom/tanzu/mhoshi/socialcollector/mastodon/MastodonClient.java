package jp.broadcom.tanzu.mhoshi.socialcollector.mastodon;

import jakarta.annotation.Nullable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("/api")
interface MastodonClient {

    @GetExchange(url="/v1/timelines/tag/{hashtag}", accept = "application/json")
    List<MastodonTimelinesResponse> getMastodonTimeLineResponses(
            @PathVariable String hashtag,
            @RequestParam Integer limit,
            @RequestParam(name = "since_id") String sinceId,
            @RequestParam(name = "max_id", required = false) @Nullable String maxId);
}