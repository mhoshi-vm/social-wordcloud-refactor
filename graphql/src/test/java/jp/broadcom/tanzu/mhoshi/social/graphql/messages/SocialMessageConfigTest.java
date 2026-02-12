package jp.broadcom.tanzu.mhoshi.social.graphql.messages;

import io.grpc.ManagedChannel;
import jp.broadcom.tanzu.mhoshi.social.analytics.proto.DeleteGrpc;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for SocialMessageConfig.
 */
class SocialMessageConfigTest {

	@Test
	void analyticsChannel_shouldCreateManagedChannel() {
		// Given
		SocialMessageConfig config = new SocialMessageConfig();

		// When
		ManagedChannel channel = config.analyticsChannel("localhost", 9090);

		// Then
		assertThat(channel).isNotNull();
		assertThat(channel.authority()).isEqualTo("localhost:9090");

		// Cleanup
		channel.shutdown();
	}

	@Test
	void deleteStub_shouldCreateBlockingStub() {
		// Given
		SocialMessageConfig config = new SocialMessageConfig();
		ManagedChannel mockChannel = mock(ManagedChannel.class);

		// When
		DeleteGrpc.DeleteBlockingStub stub = config.deleteStub(mockChannel);

		// Then
		assertThat(stub).isNotNull();
	}

}
