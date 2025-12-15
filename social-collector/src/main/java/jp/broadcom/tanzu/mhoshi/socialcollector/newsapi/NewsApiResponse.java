package jp.co.broadcom.tanzu.socialwordcloud.collector.newsapi;

import java.util.List;

record NewsApiResponse(String status, Integer totalResults, List<NewsApiResponseArticles> articles) {
	record NewsApiResponseArticles(NewsApiResponseSource source, String author, String description, String url,
			String publishedAt) {
		record NewsApiResponseSource(String id, String name) {
		}
	}
}
