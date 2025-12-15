package jp.broadcom.tanzu.mhoshi.socialcollector.mastodon;

import jp.broadcom.tanzu.mhoshi.socialcollector.shared.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@ConditionalOnProperty(value = "mastodon.enabled", havingValue = "true")
class MastodonService {

    MastodonClient mastodonClient;
    OffsetStoreRepository offsetStoreRepository;
    MastodonProperties mastodonProperties;
    String sinceId;

    MastodonService(MastodonClient mastodonClient, OffsetStoreRepository offsetStoreRepository, MastodonProperties mastodonProperties) {
        this.mastodonClient = mastodonClient;
        this.offsetStoreRepository = offsetStoreRepository;
        this.mastodonProperties = mastodonProperties;
        this.sinceId = offsetStoreRepository.findById(CollectorType.MASTODON).isPresent()
                ? offsetStoreRepository.findById(CollectorType.MASTODON).get().getPointer() : "0";
    }

    @Bean
    Supplier<List<SocialMessage>> pollMastodon(MastodonProperties mastodonProperties) {
        return () -> {
            List<MastodonTimelinesResponse> mastodonTimelinesResponses = mastodonTimelinesResponses(mastodonProperties.hashTag(), mastodonProperties.pollingLimit(), sinceId, null);
            if (!mastodonTimelinesResponses.isEmpty()) {
                this.sinceId = mastodonTimelinesResponses.getFirst().id();
                OffsetStore offsetStore = new OffsetStore();
                offsetStore.setCollector(CollectorType.MASTODON);
                offsetStore.setPointer(this.sinceId);
                offsetStoreRepository.save(offsetStore);
            }
            return mastodonTimelinesResponses.stream()
                    .map(s -> new SocialMessage(s.id(), "mastodon", s.content(), s.language(), s.account().display_name(),
                            s.url(), s.created_at(), null, null, EventAction.INSERT))
                    .toList();
        };
    }

    List<MastodonTimelinesResponse> mastodonTimelinesResponses(String hashTag, Integer limit, String sinceId, String maxId) {
        List<MastodonTimelinesResponse> mastodonTimelinesResponses = mastodonClient.getMastodonTimeLineResponses(hashTag, limit, this.sinceId, maxId);
        if(!sinceId.equals("0") && mastodonTimelinesResponses.size() == limit ){
            mastodonTimelinesResponses.addAll(mastodonTimelinesResponses(hashTag, limit, sinceId, mastodonTimelinesResponses.getLast().id()));
        }
        return mastodonTimelinesResponses;
    }
}
