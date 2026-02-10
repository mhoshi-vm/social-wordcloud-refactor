package jp.broadcom.tanzu.mhoshi.social.collector.newsapi;

import java.util.List;

record NewsApiResponse(
// @formatter:off
		String status,
		Integer totalResults,
		List<NewsApiResponseArticles> articles
		// @formatter:on
) {

	record NewsApiResponseArticles(
	// @formatter:off
			NewsApiResponseSource source,
			String author,
			String description,
			String url,
			String publishedAt,
			String content
			// @formatter:on
	) {
		record NewsApiResponseSource(
		// @formatter:off
				String id,
				String name
				// @formatter:on
		) {
		}
	}
}
