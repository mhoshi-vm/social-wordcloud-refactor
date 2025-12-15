package jp.broadcom.tanzu.mhoshi.socialcollector.newsapi;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
interface NewsApiClient {

    @GetExchange("/v2/everything")
    NewsApiResponse getEveryNews(
            @RequestParam String apiKey,
            @RequestParam Integer pageSize,
            @RequestParam String q,
            @RequestParam String language,
            @RequestParam String from,
            @RequestParam String excludeDomains,
            @RequestParam String sortBy,
            @RequestParam Integer pageNum
    );

}
