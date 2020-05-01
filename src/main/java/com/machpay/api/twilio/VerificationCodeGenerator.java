package com.machpay.api.twilio;

import java.security.SecureRandom;
import java.util.Random;

public class VerificationCodeGenerator {
    private static final int MAX_VERIFICATION_CODE = 99999;

    private static final int MIN_VERIFICATION_CODE = 10000;

    private VerificationCodeGenerator() {
        throw new IllegalStateException("Utility class");
    }

    public static String generate() {
        Random rand = new SecureRandom();
        int code = rand.nextInt(MAX_VERIFICATION_CODE
                - MIN_VERIFICATION_CODE + 1) + MAX_VERIFICATION_CODE;

        return Integer.toString(code);
    }
}
