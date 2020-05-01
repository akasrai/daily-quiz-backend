package com.machpay.api.security;

import com.machpay.api.user.auth.dto.LoginRequest;
import com.machpay.api.util.HttpServletRequestUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        byte[] bytes = HttpServletRequestUtils.getRequestReaderByte(request);
        LoginRequest authRequest = HttpServletRequestUtils.getAuthRequest(bytes);
        UsernamePasswordAuthenticationToken token
                = new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword());
        setDetails(request, token);

        return this.getAuthenticationManager().authenticate(token);
    }
}
