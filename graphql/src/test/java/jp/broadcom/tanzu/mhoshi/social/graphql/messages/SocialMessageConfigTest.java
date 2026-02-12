package jp.broadcom.tanzu.mhoshi.social.graphql.messages;

import graphql.schema.DataFetchingEnvironment;
import jp.broadcom.tanzu.mhoshi.social.analytics.proto.DeleteGrpc;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.query.SortStrategy;
import org.springframework.grpc.client.GrpcChannelFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SocialMessageConfig.
 */
class SocialMessageConfigTest {

	@Test
	void sortStrategy_shouldCreateValidStrategy() {
		// Given
		SocialMessageConfig config = new SocialMessageConfig();
		DataFetchingEnvironment mockEnv = mock(DataFetchingEnvironment.class);
		when(mockEnv.getArgument("sort")).thenReturn(List.of("name", "createDateTime"));
		when(mockEnv.getArgument("direction")).thenReturn("DESC");

		// When
		SortStrategy strategy = config.sortStrategy();
		Sort sort = strategy.extract(mockEnv);

		// Then
		assertThat(strategy).isNotNull();
		assertThat(sort).isNotNull();
		assertThat(sort.getOrderFor("name")).isNotNull();
		assertThat(sort.getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.DESC);
	}

	@Test
	void deleteStub_shouldCreateBlockingStub() {
		// Given
		SocialMessageConfig config = new SocialMessageConfig();
		GrpcChannelFactory mockChannelFactory = mock(GrpcChannelFactory.class);
		io.grpc.ManagedChannel mockChannel = mock(io.grpc.ManagedChannel.class);
		when(mockChannelFactory.createChannel("local")).thenReturn(mockChannel);

		// When
		DeleteGrpc.DeleteBlockingStub stub = config.deleteStub(mockChannelFactory);

		// Then
		assertThat(stub).isNotNull();
	}

}
