package jp.broadcom.tanzu.mhoshi.social.restapi.messages;

import org.springframework.ai.tool.annotation.Tool;
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

	@GetMapping
	@Tool(description = "Get all social message analysis data including sentiment labels, cluster IDs, and geographic locations")
	List<SocialMessageAnalysis> getAll() {
		return socialMessageAnalysisService.listAll();
	}

}
