package jp.broadcom.tanzu.mhoshi.socialcollector.apifylinkedin;

import jp.broadcom.tanzu.mhoshi.socialcollector.shared.EventAction;
import jp.broadcom.tanzu.mhoshi.socialcollector.shared.SocialMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
@EnableConfigurationProperties({ApifyLinkedInProperties.class})
@ImportHttpServices(group = "apifylinkedin", types = ApifyLinkedInClient.class)
class ApifyLinkedInClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(ApifyLinkedInClientConfig.class);

    public ApifyLinkedInClientConfig() {
    }

    @Bean
    RestClientHttpServiceGroupConfigurer groupLinkedInConfigurer(ApifyLinkedInProperties apifyLinkedInProperties) {
        return groups -> groups.filterByName("apifylinkedin").forEachClient((group, builder) -> builder
                .baseUrl(UriComponentsBuilder.newInstance()
                        .scheme(apifyLinkedInProperties.scheme())
                        .host(apifyLinkedInProperties.url())
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
    Supplier<List<ApifyLinkedInResponse>> pollLinkedIn(ApifyLinkedInClient apifyLinkedInClient, ApifyLinkedInProperties apifyLinkedInProperties) {

        return () -> {
            ApifyLinkedInRequest apifyLinkedInRequest = new ApifyLinkedInRequest(apifyLinkedInProperties.keyword(), apifyLinkedInProperties.pollingLimit(), apifyLinkedInProperties.sortType());
            return apifyLinkedInClient.apifyLinkedInResponses(apifyLinkedInProperties.appId(), apifyLinkedInProperties.token(), apifyLinkedInRequest);
        };
    }

    @Bean
    Function<List<ApifyLinkedInResponse>, List<SocialMessage>> convertLinkedInResponse() {
        return (in) -> in.stream()
                .map(s -> new SocialMessage(s.activity_id(), "linkedIn", s.text(), "en", s.author().name(),
                        s.post_url(), LocalDateTime.ofInstant(s.posted_at().timestamp(), ZoneId.of("UTC")), EventAction.INSERT))
                .toList();
    }

}
