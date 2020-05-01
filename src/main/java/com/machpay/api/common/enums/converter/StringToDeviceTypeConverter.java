package com.machpay.api.common.enums.converter;

import com.machpay.api.common.enums.ContactType;
import org.springframework.core.convert.converter.Converter;

public class StringToDeviceTypeConverter implements Converter<String, ContactType> {
    @Override
    public ContactType convert(String source) {
        try {
            return ContactType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
