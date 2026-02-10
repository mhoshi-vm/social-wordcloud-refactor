package jp.broadcom.tanzu.mhoshi.social.collector.mastodon;

import jp.broadcom.tanzu.mhoshi.social.collector.shared.CollectorType;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.OffsetStore;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.OffsetStoreRepository;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.SensitiveDataMasker;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.SocialMessage;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.*;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
@EnableConfigurationProperties({ MastodonProperties.class })
@ImportHttpServices(group = "mastodon", types = MastodonClient.class)
class MastodonClientConfig {

	private static final Logger logger = LoggerFactory.getLogger(MastodonClientConfig.class);

	String sinceId;

	public MastodonClientConfig(OffsetStoreRepository offsetStoreRepository) {
		this.sinceId = offsetStoreRepository.findById(CollectorType.MASTODON).isPresent()
				? offsetStoreRepository.findById(CollectorType.MASTODON).get().getPointer() : "0";
	}

	@Bean
	RestClientHttpServiceGroupConfigurer groupMastodonConfigurer(MastodonProperties mastodonProperties) {
		return groups -> groups.filterByName("mastodon")
			.forEachClient((group, builder) -> builder
				.baseUrl(UriComponentsBuilder.newInstance()
					.scheme(mastodonProperties.scheme())
					.host(mastodonProperties.url())
					.port(mastodonProperties.port())
					.build()
					.toUriString())
				.defaultHeaders(httpHeaders -> httpHeaders.set("Authorization", "Bearer " + mastodonProperties.token()))
				.requestInterceptor(((request, body, execution) -> {
					logger.info("Intercepting request - URI: {}", SensitiveDataMasker.maskUri(request.getURI()));
					logger.info("Headers: {}", SensitiveDataMasker.maskHeaders(request.getHeaders()));
					logger.info("Method: {}", request.getMethod());
					return execution.execute(request, body);
				}))
				.build());
	}

	@Bean
	Supplier<List<MastodonTimelinesResponse>> pollMastodon(MastodonClient mastodonClient,
			MastodonProperties mastodonProperties, OffsetStoreRepository offsetStoreRepository) {

		return () -> {
			List<MastodonTimelinesResponse> mastodonTimelinesResponses = mastodonTimelinesResponses(mastodonClient,
					mastodonProperties.hashTag(), mastodonProperties.pollingLimit(), sinceId, null);
			if (!mastodonTimelinesResponses.isEmpty()) {
				sinceId = mastodonTimelinesResponses.getFirst().id();
				offsetStoreRepository.save(new OffsetStore(CollectorType.MASTODON, sinceId));
			}
			return mastodonTimelinesResponses;
		};
	}

	@Bean
	Function<List<MastodonTimelinesResponse>, List<SocialMessage>> convertMastodonTimelinesResponse() {
		return (in) -> in.stream()
			.map(s -> new SocialMessage(s.id(), "mastodon", s.content(), s.language(), s.account().display_name(),
					s.url(), s.created_at()))
			.toList();
	}

	List<MastodonTimelinesResponse> mastodonTimelinesResponses(MastodonClient mastodonClient, String hashTag,
			Integer limit, String sinceId, @Nullable String maxId) {
		List<MastodonTimelinesResponse> mastodonTimelinesResponses = mastodonClient
			.getMastodonTimeLineResponses(hashTag, limit, sinceId, maxId);
		if (!sinceId.equals("0") && mastodonTimelinesResponses.size() == limit) {
			mastodonTimelinesResponses.addAll(mastodonTimelinesResponses(mastodonClient, hashTag, limit, sinceId,
					mastodonTimelinesResponses.getLast().id()));
		}
		return mastodonTimelinesResponses;
	}

}
