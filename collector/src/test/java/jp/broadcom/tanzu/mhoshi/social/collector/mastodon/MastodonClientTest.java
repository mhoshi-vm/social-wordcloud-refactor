package jp.broadcom.tanzu.mhoshi.social.collector.mastodon;


import jp.broadcom.tanzu.mhoshi.social.collector.TestContainersConfiguration;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.CollectorType;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.OffsetStoreRepository;
import jp.broadcom.tanzu.mhoshi.social.collector.shared.SocialMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@Import(TestContainersConfiguration.class)
class MastodonClientTest {

    @MockitoBean
    private MastodonClient mastodonClient;

    @Autowired
    private MastodonClientConfig mastodonClientConfig;

    @Autowired
    private Supplier<List<MastodonTimelinesResponse>> pollMastodon;

    @Autowired
    private Function<List<MastodonTimelinesResponse>, List<SocialMessage>> convertMastodonTimelinesResponse;

    @Autowired
    private OffsetStoreRepository offsetStoreRepository;

    @Autowired
    private MastodonProperties mastodonProperties;

    @BeforeEach
    void setUp() {
        offsetStoreRepository.deleteAll();
    }

    @Test
    void pollMastodon_ShouldFetchAndSaveOffset() {
        // Arrange
        String hashtag = mastodonProperties.hashTag();
        int limit = mastodonProperties.pollingLimit();

        MastodonTimelinesResponse.MastodonTimelinesResponseAccount account =
                new MastodonTimelinesResponse.MastodonTimelinesResponseAccount("user", "User Name");

        MastodonTimelinesResponse response1 = new MastodonTimelinesResponse(
                "100", "Content 1", "en", "http://url1", LocalDateTime.now(), account);

        Mockito.when(mastodonClient.getMastodonTimeLineResponses(eq(hashtag), eq(limit), any(), any()))
                .thenReturn(List.of(response1));

        // Act
        List<MastodonTimelinesResponse> result = pollMastodon.get();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("100");

        // Verify OffsetStore was updated
        var offset = offsetStoreRepository.findById(CollectorType.MASTODON);
        assertThat(offset).isPresent();
        assertThat(offset.get().getPointer()).isEqualTo("100");
    }

    @Test
    void convertMastodonTimelinesResponse_ShouldConvertToSocialMessage() {
        // Arrange
        var now = LocalDateTime.now();
        MastodonTimelinesResponse.MastodonTimelinesResponseAccount account =
                new MastodonTimelinesResponse.MastodonTimelinesResponseAccount("user", "User Name");
        MastodonTimelinesResponse response = new MastodonTimelinesResponse(
                "123", "Some content", "en", "http://example.com/123", now, account);

        // Act
        List<SocialMessage> messages = convertMastodonTimelinesResponse.apply(List.of(response));

        // Assert
        assertThat(messages).hasSize(1);
        SocialMessage message = messages.getFirst();
        assertThat(message.id()).isEqualTo("123");
        assertThat(message.origin()).isEqualTo("mastodon");
        assertThat(message.text()).isEqualTo("Some content");
        assertThat(message.lang()).isEqualTo("en");
        assertThat(message.name()).isEqualTo("User Name");
        assertThat(message.url()).isEqualTo("http://example.com/123");
        assertThat(message.createDateTime()).isEqualTo(now);
    }

    @Test
    void mastodonTimelinesResponses_ShouldRecursivelyFetch_WhenLimitIsReached() {
        // Arrange
        String hashtag = "testTag";
        int limit = 2; // Set a small limit to easily test pagination
        String sinceId = "500"; // specific sinceId required to enable recursion (!="0")

        // Helper to create a dummy response object
        MastodonTimelinesResponse.MastodonTimelinesResponseAccount account =
                new MastodonTimelinesResponse.MastodonTimelinesResponseAccount("user", "User");

        // Page 1: Returns 'limit' number of items (2 items). IDs: "100", "99"
        // This simulates a full page, which should trigger the recursion.
        List<MastodonTimelinesResponse> page1 = new ArrayList<>();
        page1.add(new MastodonTimelinesResponse("100", "Post 1", "en", "url1", LocalDateTime.now(), account));
        page1.add(new MastodonTimelinesResponse("99", "Post 2", "en", "url2", LocalDateTime.now(), account));

        // Page 2: Returns less than 'limit' items (1 item). ID: "98"
        // This simulates the end of the list or a partial page, stopping recursion.
        List<MastodonTimelinesResponse> page2 = new ArrayList<>();
        page2.add(new MastodonTimelinesResponse("98", "Post 3", "en", "url3", LocalDateTime.now(), account));

        // Mock the first call: maxId is null initially
        Mockito.when(mastodonClient.getMastodonTimeLineResponses(eq(hashtag), eq(limit), eq(sinceId), isNull()))
                .thenReturn(page1);

        // Mock the recursive call: maxId should be the ID of the last item from page 1 ("99")
        Mockito.when(mastodonClient.getMastodonTimeLineResponses(eq(hashtag), eq(limit), eq(sinceId), eq("99")))
                .thenReturn(page2);

        // Act
        // We call the package-private method directly since we are in the same package in src/test
        List<MastodonTimelinesResponse> result = mastodonClientConfig.mastodonTimelinesResponses(
                mastodonClient, hashtag, limit, sinceId, null
        );

        // Assert
        // Total size should be 3 (2 from page 1 + 1 from page 2)
        assertThat(result).hasSize(3);

        // Verify order is preserved
        assertThat(result.get(0).id()).isEqualTo("100");
        assertThat(result.get(1).id()).isEqualTo("99");
        assertThat(result.get(2).id()).isEqualTo("98");

        // Verify that the client was indeed called twice with the expected parameters
        Mockito.verify(mastodonClient).getMastodonTimeLineResponses(hashtag, limit, sinceId, null);
        Mockito.verify(mastodonClient).getMastodonTimeLineResponses(hashtag, limit, sinceId, "99");
    }
}