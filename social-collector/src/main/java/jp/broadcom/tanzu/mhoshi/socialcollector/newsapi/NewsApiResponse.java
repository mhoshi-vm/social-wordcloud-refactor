package jp.broadcom.tanzu.mhoshi.socialcollector.newsapi;

import java.util.List;

record NewsApiResponse(String status, Integer totalResults, List<NewsApiResponseArticles> articles) {

    record NewsApiResponseArticles(NewsApiResponseSource source, String author, String description, String url,
                                   String publishedAt, String content) {
        record NewsApiResponseSource(String id, String name) {
        }
    }
}
