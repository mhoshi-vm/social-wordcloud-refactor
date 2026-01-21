package jp.broadcom.tanzu.mhoshi.socialanalytics;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jp.broadcom.tanzu.mhoshi.socialanalytics.proto.DeleteGrpc;
import jp.broadcom.tanzu.mhoshi.socialanalytics.proto.DeleteReply;
import jp.broadcom.tanzu.mhoshi.socialanalytics.proto.DeleteRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class AnalyticsGrpcService extends DeleteGrpc.DeleteImplBase {

    AnalyticsComponent analyticsComponent;

    AnalyticsGrpcService(AnalyticsComponent analyticsComponent) {
        this.analyticsComponent = analyticsComponent;
    }

    @Override
    public void deleteMessages(DeleteRequest request, StreamObserver<DeleteReply> responseObserver) {
        List<String> ids = request.getIdsList();
        try {
            analyticsComponent.deleteSocialMessages(ids);
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Delete failed: " + e.getMessage())
                            .withCause(e)
                            .asRuntimeException()
            );
            return;
        }
        DeleteReply reply = DeleteReply.newBuilder().setMessage("Deleted").build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
