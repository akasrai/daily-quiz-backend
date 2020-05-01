package com.machpay.api.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class MailRequestException extends RuntimeException {
    public MailRequestException(String message) {
        super(message);
    }

    public MailRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
