package jp.broadcom.tanzu.mhoshi.socialrestapi.stock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
class StockWriter {

    StockService stockService;

    public StockWriter(StockService stockService) {
        this.stockService = stockService;
    }

    @Bean
    Consumer<StockEntity> stockConsumer(){
        return stockEntity -> {stockService.save(stockEntity);};
    }
}
