package com.machpay.api.common;

public final class Constants {
    public static final int MAX_LOGIN_LIMIT = 3;
    public static final int RESEND_VERIFICATION_CODE_LIMIT = 3;
    public static final String DEFAULT_PHONE_VERIFICATION_CODE = "1234";
    public static final String MAX_LOGIN_LIMIT_EXCEEDED = "Maximum login limit exceeded";
    public static final String RESEND_VERIFICATION_CODE_LIMIT_EXCEEDED = "Resending verification code limit exceeded";
    public static final String RESET_PASS = "Reset Password";
    public static final String VERIFY_EMAIL_ID = "Activate your account";
    public static final String PARSE_ERROR = "Something went wrong while parsing /login request body";

    private Constants() {
        throw new IllegalStateException("Constant class");
    }
}

