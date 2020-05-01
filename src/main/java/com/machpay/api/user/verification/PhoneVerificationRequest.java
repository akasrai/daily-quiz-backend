package com.machpay.api.user.verification;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PhoneVerificationRequest {
    @NotBlank
    private String verificationCode;
}
