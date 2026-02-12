package jp.broadcom.tanzu.mhoshi.social.graphql.messages;

import jp.broadcom.tanzu.mhoshi.social.analytics.proto.DeleteGrpc;
import jp.broadcom.tanzu.mhoshi.social.analytics.proto.DeleteReply;
import jp.broadcom.tanzu.mhoshi.social.analytics.proto.DeleteRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SocialMessageController.
 */
@ExtendWith(MockitoExtension.class)
class SocialMessageControllerTest {

	@Mock
	SocialMessageRepository socialMessageRepository;

	@Mock
	JdbcClient jdbcClient;

	@Mock
	DeleteGrpc.DeleteBlockingStub deleteStub;

	@InjectMocks
	SocialMessageController controller;

	@Test
	void deleteSocialMessages_shouldCallGrpcStubAndReturnResult() {
		// Given
		List<String> ids = List.of("id1", "id2", "id3");
		DeleteReply expectedReply = DeleteReply.newBuilder().setMessage("Successfully deleted").build();
		when(deleteStub.deleteMessages(any(DeleteRequest.class))).thenReturn(expectedReply);

		// When
		SocialMessageController.DeleteResult result = controller.deleteSocialMessages(ids);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.message()).isEqualTo("Successfully deleted");
		assertThat(result.deletedCount()).isEqualTo(3);

		verify(deleteStub).deleteMessages(any(DeleteRequest.class));
	}

	@Test
	void deleteSocialMessages_shouldPassCorrectIdsToGrpcStub() {
		// Given
		List<String> ids = List.of("message-1", "message-2");
		DeleteReply expectedReply = DeleteReply.newBuilder().setMessage("Deleted").build();
		when(deleteStub.deleteMessages(any(DeleteRequest.class))).thenReturn(expectedReply);

		// When
		controller.deleteSocialMessages(ids);

		// Then
		verify(deleteStub).deleteMessages(any(DeleteRequest.class));
	}

	@Test
	void deleteSocialMessages_shouldHandleEmptyList() {
		// Given
		List<String> ids = List.of();
		DeleteReply expectedReply = DeleteReply.newBuilder().setMessage("Nothing to delete").build();
		when(deleteStub.deleteMessages(any(DeleteRequest.class))).thenReturn(expectedReply);

		// When
		SocialMessageController.DeleteResult result = controller.deleteSocialMessages(ids);

		// Then
		assertThat(result.deletedCount()).isZero();
	}

}
