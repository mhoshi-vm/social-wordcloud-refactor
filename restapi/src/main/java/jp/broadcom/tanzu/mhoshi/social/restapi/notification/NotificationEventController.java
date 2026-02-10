package jp.broadcom.tanzu.mhoshi.social.restapi.notification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifier")
class NotificationEventController {

	NotificationService notificationService;

	public NotificationEventController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping()
	SseEmitter newMessage() {
		SseEmitter sseEmitter = new SseEmitter(-1L);
		List<SseEmitter> emitters = notificationService.getEmitters();
		emitters.add(sseEmitter);
		sseEmitter.onCompletion(() -> emitters.remove(sseEmitter));

		return sseEmitter;
	}

}
