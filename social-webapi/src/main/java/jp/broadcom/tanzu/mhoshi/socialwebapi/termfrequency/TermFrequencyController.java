package jp.broadcom.tanzu.mhoshi.socialwebapi.termfrequency;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class TermFrequencyController {

    TermFrequencyService termFrequencyService;
    TermFrequencyController(TermFrequencyService termFrequencyService) {
        this.termFrequencyService = termFrequencyService;
    }

    @GetMapping
    List<TermFrequency> getTermFrequencyEntity(Duration duration) {
        if (duration != null) {
            if (duration.equals(Duration.DAY)){
                return termFrequencyService.getTermFrequencyEntityDay();
            } else if (duration.equals(Duration.WEEK)) {
                return termFrequencyService.getTermFrequencyEntityWeek();
            }else {
                return termFrequencyService.getTermFrequencyEntityMonth();
            }
        }
        return null;
    }

}
