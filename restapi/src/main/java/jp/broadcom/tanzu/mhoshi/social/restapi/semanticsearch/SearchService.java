package jp.broadcom.tanzu.mhoshi.social.restapi.semanticsearch;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class SearchService {

	SearchRepo searchRepo;

	EmbeddingModel embeddingModel;

	public SearchService(SearchRepo searchRepo, EmbeddingModel embeddingModel) {
		this.searchRepo = searchRepo;
		this.embeddingModel = embeddingModel;
	}

	List<SearchResult> vectorStoreList(String searchString, Duration duration) {

		EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(searchString));
		return searchRepo.semanticSearch(embeddingResponse.getResult().getOutput(), duration.getValue());
	}

}
