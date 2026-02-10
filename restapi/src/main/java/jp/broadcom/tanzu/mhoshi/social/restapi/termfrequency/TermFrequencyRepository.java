package jp.broadcom.tanzu.mhoshi.social.restapi.termfrequency;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface TermFrequencyRepository extends CrudRepository<TermFrequency, Integer> {

	@Query("select * from term_frequency_entity_day;")
	List<TermFrequency> termFrequencyEntityDay();

	@Query("select * from term_frequency_entity_week;")
	List<TermFrequency> termFrequencyEntityWeek();

	@Query("select * from term_frequency_entity_month;")
	List<TermFrequency> termFrequencyEntityMonth();

}
