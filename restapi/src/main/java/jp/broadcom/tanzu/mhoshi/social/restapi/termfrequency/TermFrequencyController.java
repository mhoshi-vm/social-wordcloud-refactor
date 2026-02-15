package jp.broadcom.tanzu.mhoshi.social.restapi.termfrequency;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/term")
class TermFrequencyController {

	TermFrequencyService termFrequencyService;

	TermFrequencyController(TermFrequencyService termFrequencyService) {
		this.termFrequencyService = termFrequencyService;
	}

	@GetMapping("{duration}")
	@Tool(description = "Get term frequency data for a specific duration (DAY, WEEK, or MONTH) showing the most common words and their frequencies")
	List<TermFrequency> getTermFrequencyEntity(
			@PathVariable @ToolParam(description = "Duration filter: DAY, WEEK, or MONTH") Duration duration) {
		if (duration != null) {
			if (duration.equals(Duration.DAY)) {
				return termFrequencyService.getTermFrequencyEntityDay();
			}
			else if (duration.equals(Duration.WEEK)) {
				return termFrequencyService.getTermFrequencyEntityWeek();
			}
			else {
				return termFrequencyService.getTermFrequencyEntityMonth();
			}
		}
		return null;
	}

}
