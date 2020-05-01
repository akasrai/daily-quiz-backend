package com.machpay.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.machpay.api.entity.User;
import com.machpay.api.user.UserService;
import com.machpay.api.user.auth.AuthService;
import com.machpay.api.user.auth.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpStatus.OK.value());
        objectMapper.writeValue(response.getWriter(), getAuthResponse(authentication));
    }

    private AuthResponse getAuthResponse(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.createAccessToken(userPrincipal.getId());

        return authService.buildAuthResponse(user, token);
    }
}
