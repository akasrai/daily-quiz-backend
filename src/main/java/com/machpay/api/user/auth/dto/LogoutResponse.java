package com.machpay.api.user.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LogoutResponse {
    private Boolean status;

    private String message;
}
