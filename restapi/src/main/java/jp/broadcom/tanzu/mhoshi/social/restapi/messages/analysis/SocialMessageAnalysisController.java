package jp.broadcom.tanzu.mhoshi.social.restapi.messages.analysis;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/messages/analysis")
class SocialMessageAnalysisController {

    SocialMessageAnalysisService socialMessageAnalysisService;

    SocialMessageAnalysisController(SocialMessageAnalysisService socialMessageAnalysisService) {
        this.socialMessageAnalysisService = socialMessageAnalysisService;
    }

    @CrossOrigin
    @GetMapping
    List<SocialMessageAnalysis> getAll(){
        return socialMessageAnalysisService.listAll();
    }
}
