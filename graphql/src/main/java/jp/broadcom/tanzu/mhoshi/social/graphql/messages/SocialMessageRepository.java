package jp.broadcom.tanzu.mhoshi.social.graphql.messages;

import jp.broadcom.tanzu.mhoshi.socialwebapi.messages.graphql.SocialMessage;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.graphql.data.GraphQlRepository;

@GraphQlRepository
interface SocialMessageRepository extends
        ListPagingAndSortingRepository<SocialMessage,String>,
        QueryByExampleExecutor<SocialMessage>,
        PagingAndSortingRepository<SocialMessage, String>{ }
