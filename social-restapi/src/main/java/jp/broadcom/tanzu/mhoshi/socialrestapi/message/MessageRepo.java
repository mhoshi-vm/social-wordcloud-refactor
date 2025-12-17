package jp.broadcom.tanzu.mhoshi.socialrestapi.message;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


interface MessageRepo extends PagingAndSortingRepository<MessageEntity, String>, ListCrudRepository<MessageEntity, String> {
}
