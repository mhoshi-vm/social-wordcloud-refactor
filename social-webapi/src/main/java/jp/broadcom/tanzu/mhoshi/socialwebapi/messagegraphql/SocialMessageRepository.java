package jp.broadcom.tanzu.mhoshi.socialwebapi.messagegraphql;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.graphql.data.GraphQlRepository;

import java.util.List;

@GraphQlRepository
interface SocialMessageRepository extends
        ListPagingAndSortingRepository<SocialMessage,String>,
        QueryByExampleExecutor<SocialMessage>,
        PagingAndSortingRepository<SocialMessage, String>{ }
