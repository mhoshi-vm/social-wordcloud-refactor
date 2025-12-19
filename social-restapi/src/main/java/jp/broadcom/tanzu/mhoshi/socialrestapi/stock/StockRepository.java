package jp.broadcom.tanzu.mhoshi.socialrestapi.stock;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface StockRepository extends ListCrudRepository<StockEntity,Long> {
}
