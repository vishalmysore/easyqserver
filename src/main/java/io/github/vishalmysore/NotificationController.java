package io.github.vishalmysore;

import io.github.vishalmysore.data.Score;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
@RequestMapping("/bs")
@RestController
public class NotificationController {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // Subscribe to notifications
    @GetMapping("/broadcast")
    public SseEmitter subscribeToNotifications() {
        SseEmitter emitter = new SseEmitter(0L); // No timeout
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }

    // Broadcast a message to all connected clients
    public void sendNotification(String action, Score score) {
        for (SseEmitter emitter : emitters) {
            try {
                String jsonMessage = "{\"action\":\"" + action + "\", \"userId\":\"" + score.getUserId() + "\", \"linkUrl\":\"" + score.getUrl() + "\", \"currentScore\":" + score.getTotalScore() + "}";
                emitter.send(SseEmitter.event().data(jsonMessage));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}

