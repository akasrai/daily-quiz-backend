package com.machpay.api.user.auth.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AuthResponse {
    private String token;

    private String tokenType = "Bearer";

    private List<String> roles;
}