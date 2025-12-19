package jp.broadcom.tanzu.mhoshi.socialrestapi.stock;

import org.springframework.stereotype.Service;

@Service
class StockService {

    StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    void save(StockEntity stockEntity){
        stockRepository.save(stockEntity);
    }

}
