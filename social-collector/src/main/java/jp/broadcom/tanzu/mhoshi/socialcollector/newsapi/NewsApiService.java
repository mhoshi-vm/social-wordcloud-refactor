package jp.broadcom.tanzu.mhoshi.socialcollector.newsapi;

import jp.broadcom.tanzu.mhoshi.socialcollector.shared.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@ConditionalOnProperty(value = "newsapi.enabled", havingValue = "true")
class NewsApiService {
    NewsApiClient newsApiClient;

    OffsetStoreRepository offsetStoreRepository;

    NewsApiProperties newsApiProperties;

    String newsApiFrom;

    public NewsApiService(NewsApiClient newsApiClient, OffsetStoreRepository offsetStoreRepository, NewsApiProperties newsApiProperties) {
        this.newsApiClient = newsApiClient;
        this.offsetStoreRepository = offsetStoreRepository;
        this.newsApiProperties = newsApiProperties;
        String weekago = LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.newsApiFrom = offsetStoreRepository.findById(CollectorType.NEWSAPI).isPresent()
                ? offsetStoreRepository.findById(CollectorType.NEWSAPI).get().getPointer() : weekago;
    }

    @Bean
    Supplier<List<SocialMessage>> newsApiSupplier() {
        return () -> {

            NewsApiResponse newsApiResponse = newsApiList();

            assert newsApiResponse.articles() != null;
            if (!newsApiResponse.articles().isEmpty()) {
                this.newsApiFrom = newsApiResponse.articles().getFirst().publishedAt();
                OffsetStore offsetStore = new OffsetStore();
                offsetStore.setCollector(CollectorType.MASTODON);
                offsetStore.setPointer(this.newsApiFrom);
                offsetStoreRepository.save(offsetStore);
            }
            return newsApiResponse.articles().stream()
                    .map(s -> new SocialMessage(UUID.nameUUIDFromBytes(s.url().getBytes()).toString(), s.source().name(),
                            s.description(), newsApiProperties.language(), s.author(), s.url(),
                            LocalDateTime.parse(s.publishedAt(), DateTimeFormatter.ISO_DATE_TIME), null, null,
                            EventAction.INSERT))
                    .toList();
        };
    }

    NewsApiResponse newsApiList() {
        return newsApiClient.getEveryNews(
                newsApiProperties.key(),
                newsApiProperties.limit(),
                newsApiProperties.query(),
                newsApiProperties.language(),
                newsApiFrom,
                newsApiProperties.excludeDomains(),
                "publishedAt", 1);
    }

}
