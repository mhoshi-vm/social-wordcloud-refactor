package jp.broadcom.tanzu.mhoshi.socialrestapi.termfrequency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class TermFrequencyService {

    Logger logger = LoggerFactory.getLogger(TermFrequencyService.class);

    TermFrequencyRepo termFrequencyRepo;

    TermFrequencyService(TermFrequencyRepo termFrequencyRepo) {
        this.termFrequencyRepo = termFrequencyRepo;
    }

    List<TermFrequencyEntity> getAll() {
        return termFrequencyRepo.findAll();
    }

}
