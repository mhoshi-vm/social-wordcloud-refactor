package jp.broadcom.tanzu.mhoshi.socialwebapi.messagegraphql;

import org.springframework.data.domain.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
class SocialMessageController {

    SocialMessageRepository socialMessageRepository;

    JdbcClient jdbcClient;

    SocialMessageController(SocialMessageRepository socialMessageRepository, JdbcClient jdbcClient) {
        this.socialMessageRepository = socialMessageRepository;
        this.jdbcClient = jdbcClient;
    }

    @MutationMapping
    void deleteCustomBatch(@Argument List<String> ids, @Argument List<LocalDateTime> createDateTimes) {
        String sql = """
                CALL delete_social_message_batch(?,?)
                """;
        jdbcClient.sql(sql)
                .param(1, ids.toArray(new String[0]))
                .param(2, null)
                .update();
    }

//    @QueryMapping
//    Window<SocialMessage> socialMessages(ScrollSubrange subrange, Optional<Sort> sort) {
//        ScrollPosition position = subrange.position().orElse(ScrollPosition.offset());
//
//        // 2. Extract the limit (how many items to fetch)
//        Limit limit = Limit.of(subrange.count().orElse(10));
//
//        // 3. Define sorting if not provided by the client
//        Sort finalSort = sort.orElse(Sort.by("create_date_time").ascending());
//
//        // 4. Return the Window from the repository
//        return socialMessageRepository.findBy(limit, position, finalSort);
//    }

}
