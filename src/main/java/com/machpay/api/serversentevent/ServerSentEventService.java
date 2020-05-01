package com.machpay.api.serversentevent;

import com.machpay.api.common.enums.ServerSentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ServerSentEventService {
    private static final Logger logger = LoggerFactory.getLogger(ServerSentEventService.class);

    private final Map<UUID, List<SseEmitter>> channels = Collections.synchronizedMap(new HashMap<>());

    public SseEmitter subscribe(UUID referenceId) {
        SseEmitter sseEmitter = new SseEmitter(300_000L);

        synchronized (this.channels) {
            if (this.channels.get(referenceId) == null) {
                final List<SseEmitter> sseEmitters = Collections.synchronizedList(new ArrayList<>());
                this.channels.put(referenceId, sseEmitters);
            }

            synchronized (this.channels.get(referenceId)) {
                this.channels.get(referenceId).add(sseEmitter);

                sseEmitter.onCompletion(() -> {
                    synchronized (this.channels) {
                        synchronized (this.channels.get(referenceId)) {
                            this.channels.get(referenceId).remove(sseEmitter);

                            if (this.channels.get(referenceId) != null && this.channels.get(referenceId).isEmpty()) {
                                this.channels.remove(referenceId);
                            }
                        }
                    }
                });
                sseEmitter.onTimeout(sseEmitter::complete);
            }
        }

        return sseEmitter;
    }

    public void emitEvent(ServerSentEvent serverSentEvent, UUID referenceId) {
        synchronized (this.channels) {
            List<SseEmitter> sseEmitters = this.channels.get(referenceId);

            if (sseEmitters != null && !sseEmitters.isEmpty()) {
                synchronized (this.channels.get(referenceId)) {
                    for (SseEmitter sseEmitter : sseEmitters) {
                        try {
                            sseEmitter.send(serverSentEvent.name(), MediaType.APPLICATION_JSON);
                            sseEmitter.complete();
                        } catch (Exception e) {
                            logger.error("Error while emitting server sent event for event [{}]",
                                    serverSentEvent.name());
                        }
                    }
                }
            }
        }
    }
}
