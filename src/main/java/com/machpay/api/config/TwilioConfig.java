package com.machpay.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "twilio")
public class TwilioConfig {
    private String accountSid;

    private String authToken;

    private String fromPhoneNumber;
}
