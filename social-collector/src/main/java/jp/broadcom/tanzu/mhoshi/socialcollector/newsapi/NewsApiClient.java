package jp.co.broadcom.tanzu.socialwordcloud.collector.newsapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
class NewsApiClient {

	private static final Logger logger = LoggerFactory.getLogger(NewsApiClient.class);

	String newsApiScheme;

	String newsApiUrl;

	NewsApiClient(@Value("${newsapi.schme:https}") String newsApiScheme,
			@Value("${newsapi.url:newsapi.org}") String newsApiUrl) {
		this.newsApiUrl = newsApiUrl;
		this.newsApiScheme = newsApiScheme;
	}

	RestClient.Builder restClient() {
		return RestClient.builder()
			.baseUrl(UriComponentsBuilder.newInstance().scheme(newsApiScheme).host(newsApiUrl).build().toUriString())
			.requestInterceptor(((request, body, execution) -> {
				logger.info("Intercepting request: " + request.getURI());
				logger.info("Headers: " + request.getHeaders());
				logger.info("Method: " + request.getMethod());
				return execution.execute(request, body);
			}));
	}

}
