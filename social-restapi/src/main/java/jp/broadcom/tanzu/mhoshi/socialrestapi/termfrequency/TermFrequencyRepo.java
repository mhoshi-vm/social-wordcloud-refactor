package jp.broadcom.tanzu.mhoshi.socialrestapi.termfrequency;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TermFrequencyRepo extends ListCrudRepository<TermFrequencyEntity, Integer> {

}
