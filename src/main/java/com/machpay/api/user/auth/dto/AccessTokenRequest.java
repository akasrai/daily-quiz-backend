package com.machpay.api.user.auth.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class AccessTokenRequest {
    @NotBlank
    private String referenceToken;
}
