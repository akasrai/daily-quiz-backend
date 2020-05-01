package com.machpay.api.user.password;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
public class ResetPasswordRequest {
    @NotNull
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,20}$", message = "Invalid password format.")
    private String newPassword;

    private String oldPassword;
}
