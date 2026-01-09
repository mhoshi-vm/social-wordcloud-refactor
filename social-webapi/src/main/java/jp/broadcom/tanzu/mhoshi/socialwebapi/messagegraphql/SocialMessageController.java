package jp.broadcom.tanzu.mhoshi.socialwebapi.messagegraphql;

import org.springframework.data.domain.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Controller;

@Controller
class SocialMessageController {

    SocialMessageRepository socialMessageRepository;

    SocialMessageController(SocialMessageRepository socialMessageRepository, JdbcClient jdbcClient) {
        this.socialMessageRepository = socialMessageRepository;
    }

    @QueryMapping
    Page<SocialMessage> socialMessages(
            @Argument String origin,
            @Argument String lang,
            @Argument String name,
            ScrollSubrange subrange,
            Sort sort) {

        Example<SocialMessage> example = Example.of(new SocialMessage(null, origin, null, lang, name, null, null ));

        OffsetScrollPosition scrollPosition = (OffsetScrollPosition) subrange.position()
                .orElse(ScrollPosition.offset());
        int limit = subrange.count().orElse(10);
        int offset = scrollPosition.isInitial() ? 0 : (int) (scrollPosition.getOffset() + 1);

        PageRequest pageable = PageRequest.of(limit != 0 ? offset / limit : 0, limit, sort);
        return socialMessageRepository.findAll(example, pageable);
    }

}
