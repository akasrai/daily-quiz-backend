package com.machpay.api.common.enums;

import lombok.Getter;

@Getter
public enum ApplicationEnvironment {
    DEV("dev"),
    UAT("uat"),
    PROD("prod");

    private String environment;

    ApplicationEnvironment(String env) {
        this.environment = env;
    }
}
