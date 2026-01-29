package jp.broadcom.tanzu.mhoshi.socialwebapi.messages.analysis;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class SocialMessageAnalysisService {

    SocialMessageAnalysisRepo socialMessageAnalysisRepo;
    SocialMessageAnalysisService(SocialMessageAnalysisRepo socialMessageAnalysisRepo) {
        this.socialMessageAnalysisRepo = socialMessageAnalysisRepo;
    }

    List<SocialMessageAnalysis> listAll() {
        return socialMessageAnalysisRepo.findAll();
    }
}
