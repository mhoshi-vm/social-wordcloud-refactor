package jp.broadcom.tanzu.mhoshi.social.graphql.messages;

import jp.broadcom.tanzu.mhoshi.social.analytics.proto.DeleteGrpc;
import jp.broadcom.tanzu.mhoshi.social.analytics.proto.DeleteReply;
import jp.broadcom.tanzu.mhoshi.social.analytics.proto.DeleteRequest;
import org.springframework.data.domain.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
class SocialMessageController {

	SocialMessageRepository socialMessageRepository;

	DeleteGrpc.DeleteBlockingStub deleteStub;

	SocialMessageController(SocialMessageRepository socialMessageRepository, DeleteGrpc.DeleteBlockingStub deleteStub) {
		this.socialMessageRepository = socialMessageRepository;
		this.deleteStub = deleteStub;
	}

	@QueryMapping
	Map<String, Object> socialMessages(@Argument String origin, @Argument String lang, @Argument String name,
			@Argument Integer offset, ScrollSubrange subrange, Sort sort) {

		Example<SocialMessage> example = Example.of(new SocialMessage(null, origin, null, lang, name, null, null));

		int limit = subrange.count().orElse(10);

		// Use explicit offset if provided, otherwise default to 0
		int actualOffset = offset != null ? offset : 0;

		PageRequest pageable = PageRequest.of(limit != 0 ? actualOffset / limit : 0, limit, sort);
		Page<SocialMessage> page = socialMessageRepository.findAll(example, pageable);

		// Build edges manually
		List<Map<String, Object>> edges = page.getContent()
			.stream()
			.map(msg -> Map.of("cursor", msg.id(), "node", msg))
			.collect(Collectors.toList());

		// Build pageInfo
		Map<String, Object> pageInfo = Map.of("hasNextPage", page.hasNext(), "hasPreviousPage", page.hasPrevious(),
				"startCursor", edges.isEmpty() ? "" : edges.getFirst().get("cursor"), "endCursor",
				edges.isEmpty() ? "" : edges.getLast().get("cursor"));

		// Build complete connection response
		return Map.of("edges", edges, "pageInfo", pageInfo, "totalCount", page.getTotalElements());
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
