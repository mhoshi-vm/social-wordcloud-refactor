package jp.broadcom.tanzu.mhoshi.socialrestapi.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
class MessageService {

    Logger logger = LoggerFactory.getLogger(MessageService.class);

    MessageRepo messageRepo;

    public MessageService(MessageRepo messageRepo) {
        this.messageRepo = messageRepo;
    }

    @Cacheable("WebSocialMessage")
    public List<MessageEntity> pageableFindAll(int pageNum, int pageSize, String sortBy) {
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(sortBy));
        return new ArrayList<>(this.messageRepo.findAll(pageable).getContent());
    }

    void delete(String id) {
        messageRepo.findById(id).ifPresent(messageRepo::delete);
    }

}
