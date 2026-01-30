package jp.broadcom.tanzu.mhoshi.social.collector.shared;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OffsetStoreRepository extends ListCrudRepository<OffsetStore, CollectorType> {

}
