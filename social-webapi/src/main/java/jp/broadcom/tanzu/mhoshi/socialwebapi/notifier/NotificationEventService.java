package jp.broadcom.tanzu.mhoshi.socialwebapi.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
class NotificationEventService {

    Logger logger = LoggerFactory.getLogger(NotificationEventService.class);

    List<SseEmitter> emitters;

    NotificationEventService() {
        this.emitters = new CopyOnWriteArrayList<>();
    }

    List<SseEmitter> getEmitters() {
        return emitters;
    }

    void notify(String messageEntity) {
            emitters.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event().name("new entry").data("New Entry : " ));
                } catch (IOException e) {
                    logger.warn("Failed to send SSE :{}", String.valueOf(e));
                }
            });
    }
}
