package jp.broadcom.tanzu.mhoshi.social.graphql;

import io.grpc.ManagedChannel;
import jp.broadcom.tanzu.mhoshi.social.analytics.proto.DeleteGrpc;
import jp.broadcom.tanzu.mhoshi.social.analytics.proto.DeleteReply;
import jp.broadcom.tanzu.mhoshi.social.analytics.proto.DeleteRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Import({ TestcontainersConfiguration.class, GraphqlApplicationTests.TestConfig.class })
@SpringBootTest
class GraphqlApplicationTests {

	@Test
	void contextLoads() {
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class TestConfig {

		@Bean
		@Primary
		ManagedChannel analyticsChannel() {
			return mock(ManagedChannel.class);
		}

		@Bean
		@Primary
		DeleteGrpc.DeleteBlockingStub deleteStub() {
			DeleteGrpc.DeleteBlockingStub stub = mock(DeleteGrpc.DeleteBlockingStub.class);
			DeleteReply reply = DeleteReply.newBuilder().setMessage("Deleted").build();
			when(stub.deleteMessages(any(DeleteRequest.class))).thenReturn(reply);
			return stub;
		}

	}

}
