package jp.broadcom.tanzu.mhoshi.social.restapi.messages.analysis;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SocialMessageAnalysisRepo extends ListCrudRepository<SocialMessageAnalysis, String> {
}
