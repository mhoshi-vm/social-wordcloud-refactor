package jp.broadcom.tanzu.mhoshi.social.collector.newsapi;

import jp.broadcom.tanzu.mhoshi.social.collector.shared.CollectorType;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.OffsetStore;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.OffsetStoreRepository;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.SocialMessage;
import jp.broadcom.tanzu.mhoshi.socialcollector.shared.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
@EnableConfigurationProperties(NewsApiProperties.class)
@ImportHttpServices(group = "newsapi", types = NewsApiClient.class)
class NewsApiClientConfig {

	private static final Logger logger = LoggerFactory.getLogger(NewsApiClientConfig.class);

	String newsApiFrom;

	public NewsApiClientConfig(OffsetStoreRepository offsetStoreRepository) {
		String oneWeekAgo = LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		this.newsApiFrom = offsetStoreRepository.findById(CollectorType.NEWSAPI).isPresent()
				? offsetStoreRepository.findById(CollectorType.NEWSAPI).get().getPointer() : oneWeekAgo;
	}

	@Bean
	RestClientHttpServiceGroupConfigurer groupNewsApiConfigurer(NewsApiProperties newsApiProperties) {
		return groups -> groups.filterByName("newsapi")
			.forEachClient((group, builder) -> builder
				.baseUrl(UriComponentsBuilder.newInstance()
					.scheme(newsApiProperties.scheme())
					.host(newsApiProperties.url())
					.build()
					.toUriString())
				.requestInterceptor(((request, body, execution) -> {
					logger.info("Intercepting request: {}", request.getURI());
					logger.info("Headers: {}", request.getHeaders());
					logger.info("Method: {}", request.getMethod());
					return execution.execute(request, body);
				}))
				.build());
	}

	@Bean
	Supplier<NewsApiResponse> newsApiSupplier(NewsApiClient newsApiClient, NewsApiProperties newsApiProperties,
			OffsetStoreRepository offsetStoreRepository) {
		return () -> {

			NewsApiResponse newsApiResponse = newsApiList(newsApiClient, newsApiProperties);
			if (!newsApiResponse.articles().isEmpty()) {
				// Add a minute from the published article to avoid the same coming
				this.newsApiFrom = Instant.parse(newsApiResponse.articles().getFirst().publishedAt())
					.plusSeconds(60)
					.toString();
				offsetStoreRepository.save(new OffsetStore(CollectorType.NEWSAPI, this.newsApiFrom));
			}
			return newsApiResponse;
		};
	}

	@Bean
	Function<NewsApiResponse, List<SocialMessage>> convertNewsApiResponse(NewsApiProperties newsApiProperties) {
		return (in) -> in.articles()
			.stream()
			.map(s -> new SocialMessage(UUID.nameUUIDFromBytes(s.url().getBytes()).toString(), s.source().name(),
					String.format("%s\n%s", s.description(), s.content()), newsApiProperties.language(), s.author(),
					s.url(), LocalDateTime.parse(s.publishedAt(), DateTimeFormatter.ISO_DATE_TIME)))
			.toList();
	}

	NewsApiResponse newsApiList(NewsApiClient newsApiClient, NewsApiProperties newsApiProperties) {
		return newsApiClient.getEveryNews(newsApiProperties.key(), newsApiProperties.limit(), newsApiProperties.query(),
				newsApiProperties.language(), newsApiFrom, newsApiProperties.excludeDomains(), "publishedAt", 1);
	}

}
