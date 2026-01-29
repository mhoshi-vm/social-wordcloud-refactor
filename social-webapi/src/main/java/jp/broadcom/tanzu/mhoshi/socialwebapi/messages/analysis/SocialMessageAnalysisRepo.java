package jp.broadcom.tanzu.mhoshi.socialwebapi.messages.analysis;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SocialMessageAnalysisRepo extends ListCrudRepository<SocialMessageAnalysis, String> {
}
