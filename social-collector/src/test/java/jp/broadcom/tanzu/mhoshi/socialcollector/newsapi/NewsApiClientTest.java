package jp.broadcom.tanzu.mhoshi.socialcollector.newsapi;

import jp.broadcom.tanzu.mhoshi.socialcollector.shared.CollectorType;
import jp.broadcom.tanzu.mhoshi.socialcollector.shared.OffsetStoreRepository;
import jp.broadcom.tanzu.mhoshi.socialcollector.shared.SocialMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class NewsApiClientTest {

    @MockitoBean
    private NewsApiClient newsApiClient;

    @Autowired
    private Supplier<NewsApiResponse> newsApiSupplier;

    @Autowired
    private Function<NewsApiResponse, List<SocialMessage>> convertNewsApiResponse;

    @Autowired
    private OffsetStoreRepository offsetStoreRepository;

    @Autowired
    private NewsApiProperties newsApiProperties;

    @BeforeEach
    void setUp() {
        offsetStoreRepository.deleteAll();
    }

    @Test
    void newsApiSupplier_ShouldFetchAndSaveNextSecondAsOffset() {
        // Arrange
        String publishedAt = "2023-10-01T10:00:00Z";
        NewsApiResponse.NewsApiResponseArticles.NewsApiResponseSource source =
                new NewsApiResponse.NewsApiResponseArticles.NewsApiResponseSource("bbc", "BBC News");

        NewsApiResponse.NewsApiResponseArticles article = new NewsApiResponse.NewsApiResponseArticles(
                source, "Author", "Desc", "http://news.com", publishedAt, "Content");

        NewsApiResponse mockResponse = new NewsApiResponse("ok", 1, List.of(article));

        Mockito.when(newsApiClient.getEveryNews(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockResponse);

        // Act
        NewsApiResponse result = newsApiSupplier.get();

        // Assert
        assertThat(result.articles()).hasSize(1);

        // Verify OffsetStore was updated to publishedAt + 1 second
        var offset = offsetStoreRepository.findById(CollectorType.NEWSAPI);
        assertThat(offset).isPresent();
        // 2023-10-01T10:00:00 + 1 second = 2023-10-01T10:00:01Z (Instant.toString representation)
        assertThat(offset.get().getPointer()).isEqualTo("2023-10-01T10:00:01Z");
    }

    @Test
    void convertNewsApiResponse_ShouldFormatContentCorrectly() {
        // Arrange
        NewsApiResponse.NewsApiResponseArticles.NewsApiResponseSource source =
                new NewsApiResponse.NewsApiResponseArticles.NewsApiResponseSource("cnn", "CNN");

        String url = "http://cnn.com/article";
        NewsApiResponse.NewsApiResponseArticles article = new NewsApiResponse.NewsApiResponseArticles(
                source, "Wolf Blitzer", "Breaking News", url, "2023-10-01T12:00:00", "Full story content");

        NewsApiResponse response = new NewsApiResponse("ok", 1, List.of(article));

        // Act
        List<SocialMessage> messages = convertNewsApiResponse.apply(response);

        // Assert
        assertThat(messages).hasSize(1);
        SocialMessage msg = messages.getFirst();

        assertThat(msg.id()).isEqualTo(UUID.nameUUIDFromBytes(url.getBytes()).toString());
        assertThat(msg.origin()).isEqualTo("CNN");
        // Check formatting: "%s\n%s" -> Description + \n + Content
        assertThat(msg.text()).isEqualTo("Breaking News\nFull story content");
        assertThat(msg.name()).isEqualTo("Wolf Blitzer");
        assertThat(msg.createDateTime()).isEqualTo(LocalDateTime.parse("2023-10-01T12:00:00", DateTimeFormatter.ISO_DATE_TIME));
    }
}