package jp.broadcom.tanzu.mhoshi.socialrestapi.termfrequency;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/term/frequency")
class TermFrequencyController {

    TermFrequencyService termFrequencyService;

    public TermFrequencyController(TermFrequencyService termFrequencyService) {
        this.termFrequencyService = termFrequencyService;
    }

    @GetMapping
    public List<TermFrequencyEntity> termFrequencies() {
        return termFrequencyService.getAll();
    }

}
