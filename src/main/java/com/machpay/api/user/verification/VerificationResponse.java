package com.machpay.api.user.verification;

import lombok.Data;

@Data
public class VerificationResponse {

    private Boolean status;

    private String message;
}
