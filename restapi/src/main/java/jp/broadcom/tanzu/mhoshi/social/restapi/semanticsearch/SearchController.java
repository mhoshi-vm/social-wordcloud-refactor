package jp.broadcom.tanzu.mhoshi.social.restapi.semanticsearch;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
@ConditionalOnProperty(name = "database", havingValue = "postgres")
class SearchController {

	SearchService vectorStoreService;

	SearchController(SearchService vectorStoreService) {
		this.vectorStoreService = vectorStoreService;
	}

	@GetMapping
	@Tool(description = "Perform semantic search on social messages using natural language query and duration filter (DAY, WEEK, or MONTH)")
	List<SearchResult> semanticSearch(
			@RequestParam @ToolParam(description = "Natural language search query") String searchString,
			@RequestParam @ToolParam(description = "Time duration filter: DAY, WEEK, or MONTH") Duration duration) {
		return vectorStoreService.vectorStoreList(searchString, duration);
	}

}
