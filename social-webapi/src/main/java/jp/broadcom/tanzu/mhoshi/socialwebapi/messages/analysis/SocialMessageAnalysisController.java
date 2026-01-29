package jp.broadcom.tanzu.mhoshi.socialwebapi.messages.analysis;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class SocialMessageAnalysisController {

    SocialMessageAnalysisService socialMessageAnalysisService;

    SocialMessageAnalysisController(SocialMessageAnalysisService socialMessageAnalysisService) {
        this.socialMessageAnalysisService = socialMessageAnalysisService;
    }

    @GetMapping
    List<SocialMessageAnalysis> getAll(){
        return socialMessageAnalysisService.listAll();
    }
}
