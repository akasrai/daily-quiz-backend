package com.machpay.api.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum ContactType {
    PHONE("phone"),
    EMAIL("email");

    private static final Map<String, ContactType> deviceTypeHashMap = Arrays.stream(ContactType.values())
            .collect(Collectors.toMap(ContactType::getType, Function.identity()));
    private String type;

    ContactType(String type) {
        this.type = type;
    }

    public static ContactType get(String value) {
        return deviceTypeHashMap.get(value);
    }
}
