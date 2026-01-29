package jp.broadcom.tanzu.mhoshi.socialwebapi.notifier;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifier")
class NotificationEventController {

    NotificationEventService service;

    NotificationEventController(NotificationEventService service) {
        this.service = service;
    }

    @GetMapping()
    SseEmitter newMessage() {
        SseEmitter sseEmitter = new SseEmitter(-1L);
        List<SseEmitter> emitters = service.getEmitters();
        emitters.add(sseEmitter);
        sseEmitter.onCompletion(() -> emitters.remove(sseEmitter));

        return sseEmitter;
    }

}
