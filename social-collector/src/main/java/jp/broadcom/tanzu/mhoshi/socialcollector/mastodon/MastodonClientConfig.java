package jp.broadcom.tanzu.mhoshi.socialcollector.mastodon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
@EnableConfigurationProperties({MastodonProperties.class})
@ImportHttpServices(MastodonClient.class)
class MastodonClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(MastodonClientConfig.class);

    @Bean
    RestClientHttpServiceGroupConfigurer groupMastodonConfigurer(MastodonProperties mastodonProperties) {
        return groups -> groups.forEachClient((group, builder) -> builder
                .baseUrl(UriComponentsBuilder.newInstance()
                        .scheme(mastodonProperties.scheme())
                        .host(mastodonProperties.url())
                        .port(mastodonProperties.port())
                        .build()
                        .toUriString())
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.set("Accept", "application/json");
                    httpHeaders.set("Authorization", "Bearer " + mastodonProperties.token());
                })
                .requestInterceptor(((request, body, execution) -> {
                    logger.info("Intercepting request: {}", request.getURI());
                    logger.info("Headers: {}", request.getHeaders());
                    logger.info("Method: {}", request.getMethod());
                    return execution.execute(request, body);
                }))
                .build());
    }

}
