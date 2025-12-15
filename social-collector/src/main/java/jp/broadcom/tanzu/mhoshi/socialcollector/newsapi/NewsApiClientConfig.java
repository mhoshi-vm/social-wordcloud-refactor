package jp.broadcom.tanzu.mhoshi.socialcollector.newsapi;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
@EnableConfigurationProperties(NewsApiProperties.class)
@ImportHttpServices(NewsApiClient.class)
class NewsApiClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(NewsApiClientConfig.class);

    @Bean
    RestClientHttpServiceGroupConfigurer groupNewsApiConfigurer(NewsApiProperties newsApiProperties) {
        return groups -> groups.forEachClient((group, builder) -> builder
                .baseUrl(UriComponentsBuilder.newInstance()
                        .scheme(newsApiProperties.scheme())
                        .host(newsApiProperties.url())
                        .build().toUriString())
                .requestInterceptor(((request, body, execution) -> {
                    logger.info("Intercepting request: {}", request.getURI());
                    logger.info("Headers: {}", request.getHeaders());
                    logger.info("Method: {}", request.getMethod());
                    return execution.execute(request, body);
                }))
                .build());
    }
}
