package jp.broadcom.tanzu.mhoshi.social.restapi.semanticsearch;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface SearchRepo extends CrudRepository<SearchResult, String> {

	@Query("""
			SELECT
			    id,
			    message_id,
			    msg_timestamp,
			    embedding <=> :embedding::vector AS distance
			FROM vector_store
			WHERE msg_timestamp >= NOW() - CAST(:interval AS INTERVAL)
			ORDER BY distance ASC
			LIMIT 10;
			""")
	List<SearchResult> semanticSearch(float[] embedding, String interval);

}
