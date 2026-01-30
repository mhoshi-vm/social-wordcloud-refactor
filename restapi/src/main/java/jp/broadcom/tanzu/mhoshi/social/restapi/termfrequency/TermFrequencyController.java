package jp.broadcom.tanzu.mhoshi.social.restapi.termfrequency;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/term")
class TermFrequencyController {

    TermFrequencyService termFrequencyService;
    TermFrequencyController(TermFrequencyService termFrequencyService) {
        this.termFrequencyService = termFrequencyService;
    }

    @CrossOrigin
    @GetMapping("{duration}")
    List<TermFrequency> getTermFrequencyEntity(@PathVariable Duration duration) {
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
