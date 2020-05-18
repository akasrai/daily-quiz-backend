package com.machpay.api.notification;

import com.machpay.api.common.enums.NotificationTopic;
import com.machpay.api.config.FirebaseConfig;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
public class PushNotificationService {
    private String apiUrl;

    private String serverKey;

    @Autowired
    public PushNotificationService(FirebaseConfig firebaseConfig) {
        this.apiUrl = firebaseConfig.getApiUrl();
        this.serverKey = firebaseConfig.getServerKey();
    }

    @Async
    public CompletableFuture<String> send(HttpEntity<String> entity) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(getClientHttpRequestInterceptors());
        String firebaseResponse = restTemplate.postForObject(this.apiUrl, entity, String.class);

        return CompletableFuture.completedFuture(firebaseResponse);
    }

    private ArrayList<ClientHttpRequestInterceptor> getClientHttpRequestInterceptors() {
        ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new HeaderRequestInterceptor("Content-Type", "application/json"));
        interceptors.add(new HeaderRequestInterceptor("Authorization", "key=" + this.serverKey));

        return interceptors;
    }

    public JSONObject getNotificationBody(String title, String message, NotificationTopic topic) {
        JSONObject body = createNotificationBody(topic);
        JSONObject notification = createNotification(title, message);
        body.put("notification", notification);

        return body;
    }

    private JSONObject createNotificationBody(NotificationTopic topic) {
        JSONObject body = new JSONObject();
        body.put("to", "/topics/" + topic.getTopic());
        body.put("priority", "high");

        return body;
    }

    private JSONObject createNotification(String title, String body) {
        JSONObject notification = new JSONObject();
        notification.put("title", title);
        notification.put("body", body);

        return notification;
    }
}
