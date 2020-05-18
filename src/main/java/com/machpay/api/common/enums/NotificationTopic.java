package com.machpay.api.common.enums;

import lombok.Getter;

@Getter
public enum NotificationTopic {
    DAILY_QUIZ("daily-quiz");

    private String topic;

    NotificationTopic(String topic) {
        this.topic = topic;
    }

}
