package jp.broadcom.tanzu.mhoshi.socialwebapi.termfrequency;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class TermFrequencyService {

    TermFrequencyRepository termFrequencyRepository;

    TermFrequencyService(TermFrequencyRepository termFrequencyRepository) {
        this.termFrequencyRepository = termFrequencyRepository;
    }

    List<TermFrequency> getTermFrequencyEntityDay() {
        return termFrequencyRepository.termFrequencyEntityDay();
    }

    List<TermFrequency> getTermFrequencyEntityWeek() {
        return termFrequencyRepository.termFrequencyEntityWeek();
    }

    List<TermFrequency> getTermFrequencyEntityMonth() {
        return termFrequencyRepository.termFrequencyEntityMonth();
    }

}
