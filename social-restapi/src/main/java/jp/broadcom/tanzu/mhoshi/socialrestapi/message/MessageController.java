package jp.broadcom.tanzu.mhoshi.socialrestapi.message;


import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@Transactional
class MessageController {

    MessageService messageService;

    MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    List<MessageEntity> getAllMessages(@RequestParam(defaultValue = "0") int pageNum,
                                       @RequestParam(defaultValue = "100") int pageSize,
                                       @RequestParam(defaultValue = "createDateTime") String sortBy) {
        return messageService.pageableFindAll(pageNum, pageSize, sortBy);
    }

    @PostMapping("/delete")
    void deleteTweet(@RequestParam String id) {
        messageService.delete(id);
    }

}
