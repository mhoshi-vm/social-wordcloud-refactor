package jp.broadcom.tanzu.mhoshi.social.graphql.messages;

import jp.broadcom.tanzu.mhoshi.social.analytics.proto.DeleteGrpc;
import jp.broadcom.tanzu.mhoshi.social.analytics.proto.DeleteReply;
import jp.broadcom.tanzu.mhoshi.social.analytics.proto.DeleteRequest;
import org.springframework.data.domain.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
class SocialMessageController {

	SocialMessageRepository socialMessageRepository;

	DeleteGrpc.DeleteBlockingStub deleteStub;

	SocialMessageController(SocialMessageRepository socialMessageRepository, JdbcClient jdbcClient,
			DeleteGrpc.DeleteBlockingStub deleteStub) {
		this.socialMessageRepository = socialMessageRepository;
		this.deleteStub = deleteStub;
	}

	@QueryMapping
	Page<SocialMessage> socialMessages(@Argument String origin, @Argument String lang, @Argument String name,
			ScrollSubrange subrange, Sort sort) {

		Example<SocialMessage> example = Example.of(new SocialMessage(null, origin, null, lang, name, null, null));

		OffsetScrollPosition scrollPosition = (OffsetScrollPosition) subrange.position()
			.orElse(ScrollPosition.offset());
		int limit = subrange.count().orElse(10);
		int offset = scrollPosition.isInitial() ? 0 : (int) (scrollPosition.getOffset() + 1);

		PageRequest pageable = PageRequest.of(limit != 0 ? offset / limit : 0, limit, sort);
		return socialMessageRepository.findAll(example, pageable);
	}

	@MutationMapping
	DeleteResult deleteSocialMessages(@Argument List<String> ids) {
		DeleteRequest request = DeleteRequest.newBuilder().addAllIds(ids).build();
		DeleteReply reply = deleteStub.deleteMessages(request);
		return new DeleteResult(reply.getMessage(), ids.size());
	}

	record DeleteResult(
	// @formatter:off
			String message,
			int deletedCount
			// @formatter:on
	) {
	}

}
