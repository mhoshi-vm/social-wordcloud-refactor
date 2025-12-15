package jp.broadcom.tanzu.mhoshi.socialcollector.mastodon;

import jp.broadcom.tanzu.mhoshi.socialcollector.shared.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Service
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
    Supplier<List<SocialMessage>> pollMastodon() {
        return () -> {
            List<MastodonTimelinesResponse> mastodonTimelinesResponses = mastodonClient.getMastodonTimeLineResponses(mastodonProperties.hashTag(), mastodonProperties.pollingLimit(), this.sinceId, null);
            List<SocialMessage> socialMessages = mastodonTimelinesResponses.stream()
                    .map(s -> new SocialMessage(s.id(), "mastodon", s.content(), s.language(), s.account().display_name(),
                            s.url(), s.created_at(), null, null, EventAction.INSERT))
                    .toList();
            if (!socialMessages.isEmpty()) {
                this.sinceId = socialMessages.getFirst().id();
                OffsetStore offsetStore = new OffsetStore();
                offsetStore.setCollector(CollectorType.MASTODON);
                offsetStore.setPointer(this.sinceId);
                offsetStoreRepository.save(offsetStore);
            }
            return socialMessages;
        };
    }
}
