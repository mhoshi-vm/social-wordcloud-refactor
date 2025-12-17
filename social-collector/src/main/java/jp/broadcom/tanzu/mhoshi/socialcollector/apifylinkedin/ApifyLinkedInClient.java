package jp.broadcom.tanzu.mhoshi.socialcollector.apifylinkedin;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@HttpExchange()
interface ApifyLinkedInClient {

    @PostExchange(url = "/v2/acts/{appId}/run-sync-get-dataset-items", accept = "application/json")
    List<ApifyLinkedInResponse> apifyLinkedInResponses(
            @PathVariable String appId,
            @RequestParam String token,
            @RequestBody ApifyLinkedInRequest body
    );
}