package jp.broadcom.tanzu.mhoshi.socialanalytics;


import io.grpc.internal.testing.StreamRecorder;
import jp.broadcom.tanzu.mhoshi.socialanalytics.proto.DeleteReply;
import jp.broadcom.tanzu.mhoshi.socialanalytics.proto.DeleteRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsGrpcServiceTest {

    @Mock
    private AnalyticsComponent analyticsComponent;

    private AnalyticsGrpcService analyticsGrpcService;

    @BeforeEach
    void setUp() {
        analyticsGrpcService = new AnalyticsGrpcService(analyticsComponent);
    }

    @Test
    void deleteMessages_ShouldCallComponentAndReturnSuccess() {
        // Arrange
        String testId = "test-id-123";
        DeleteRequest request = DeleteRequest.newBuilder()
                .addIds(testId)
                .build();
        StreamRecorder<DeleteReply> responseObserver = StreamRecorder.create();

        // Act
        analyticsGrpcService.deleteMessages(request, responseObserver);

        // Assert
        verify(analyticsComponent).deleteSocialMessages(List.of(testId));
        assertThat(responseObserver.getError()).isNull();
        List<DeleteReply> results = responseObserver.getValues();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMessage()).isEqualTo("Deleted");
    }

    @Test
    void deleteMessages_ShouldReturnErrorOnException() {
        // Arrange
        DeleteRequest request = DeleteRequest.newBuilder().addIds("error-id").build();
        StreamRecorder<DeleteReply> responseObserver = StreamRecorder.create();

        doThrow(new RuntimeException("Database error"))
                .when(analyticsComponent).deleteSocialMessages(anyList());

        // Act
        analyticsGrpcService.deleteMessages(request, responseObserver);

        // Assert
        assertThat(responseObserver.getError()).isNotNull();
        assertThat(responseObserver.getError().getMessage()).contains("Database error");
    }
}