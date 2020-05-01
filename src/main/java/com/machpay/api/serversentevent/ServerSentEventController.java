package com.machpay.api.serversentevent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;


@RestController
@RequestMapping("/v1/events")
public class ServerSentEventController {

    @Autowired
    private ServerSentEventService serverSentEventService;

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam(value = "channel") UUID referenceId) {
        return serverSentEventService.subscribe(referenceId);
    }
}
