package jp.broadcom.tanzu.mhoshi.social.collector.stocksapi;

import jp.broadcom.tanzu.mhoshi.social.collector.shared.SensitiveDataMasker;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.SocialMessage;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
@EnableConfigurationProperties({ StocksApiProperties.class })
@ImportHttpServices(group = "stocksapi", types = StocksApiClient.class)
class StocksApiClientConfig {

	private static final Logger logger = LoggerFactory.getLogger(StocksApiClientConfig.class);

	@Bean
	RestClientHttpServiceGroupConfigurer groupStocksApiConfigurer(StocksApiProperties stocksApiProperties) {
		return groups -> groups.filterByName("stocksapi")
			.forEachClient((group, builder) -> builder
				.baseUrl(UriComponentsBuilder.newInstance()
					.scheme(stocksApiProperties.scheme())
					.host(stocksApiProperties.url())
					.build()
					.toUriString())
				.defaultHeaders(httpHeaders -> httpHeaders.set("X-Api-Key", stocksApiProperties.apiKey()))
				.requestInterceptor(((request, body, execution) -> {
					logger.info("Intercepting request - URI: {}", SensitiveDataMasker.maskUri(request.getURI()));
					logger.info("Headers: {}", SensitiveDataMasker.maskHeaders(request.getHeaders()));
					logger.info("Method: {}", request.getMethod());
					return execution.execute(request, body);
				}))
				.build());
	}

	@Bean
	Supplier<StockPriceResponse> pollStocksApi(StocksApiClient stocksApiClient,
			StocksApiProperties stocksApiProperties) {

		return () -> stocksApiClient.getStockPriceResponses(stocksApiProperties.ticker());
	}

	@Bean
	Function<StockPriceResponse, List<SocialMessage>> convertStocksApi(StocksApiProperties socialApiProperties,
			StocksApiProperties stocksApiProperties, ObjectMapper objectMapper) {
		return (in) -> {

			SocialMessage socialMessage = new SocialMessage(
					UUID.nameUUIDFromBytes(("stocksprice:" + in.updated()).getBytes()).toString(), "stocksprice",
					objectMapper.writeValueAsString(in), "en", stocksApiProperties.ticker(), socialApiProperties.url(),
					LocalDateTime.ofInstant(in.updated(), ZoneId.of("UTC")));

			return List.of(socialMessage);
		};
	}

}
