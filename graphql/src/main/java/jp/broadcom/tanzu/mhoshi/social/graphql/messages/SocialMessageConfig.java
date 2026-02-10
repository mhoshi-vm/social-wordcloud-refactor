package jp.broadcom.tanzu.mhoshi.social.graphql.messages;

import graphql.schema.DataFetchingEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.query.AbstractSortStrategy;
import org.springframework.graphql.data.query.SortStrategy;

import java.util.List;

@Configuration
class SocialMessageConfig {

	@Bean
	SortStrategy sortStrategy() {
		return new AbstractSortStrategy() {
			@Override
			protected List<String> getProperties(DataFetchingEnvironment environment) {
				// Extracts the list of field names from the 'sort' argument
				return environment.getArgument("sort");
			}

			@Override
			protected Sort.Direction getDirection(DataFetchingEnvironment environment) {
				// Extracts the sort direction (e.g., ASC, DESC) from the 'direction'
				// argument
				return Sort.Direction.fromOptionalString(environment.getArgument("direction")).orElse(null);
			}
		};
	}

}
