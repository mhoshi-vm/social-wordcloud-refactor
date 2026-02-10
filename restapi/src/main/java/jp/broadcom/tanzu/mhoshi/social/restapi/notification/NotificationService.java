package jp.broadcom.tanzu.mhoshi.social.restapi.notification;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
class NotificationService {

	List<SseEmitter> emitters;

	public NotificationService(List<SseEmitter> emitters) {
		this.emitters = new CopyOnWriteArrayList<>(emitters);
	}

	public List<SseEmitter> getEmitters() {
		return emitters;
	}

}
