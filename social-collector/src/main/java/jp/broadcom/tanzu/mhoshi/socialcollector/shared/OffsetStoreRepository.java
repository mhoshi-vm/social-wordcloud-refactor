package jp.broadcom.tanzu.mhoshi.socialcollector.shared;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OffsetStoreRepository extends ListCrudRepository<OffsetStore, CollectorType> {

}
