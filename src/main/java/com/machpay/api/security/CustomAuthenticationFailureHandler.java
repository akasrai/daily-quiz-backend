package com.machpay.api.security;

import com.machpay.api.common.Constants;
import com.machpay.api.common.Messages;
import com.machpay.api.common.enums.AuthProvider;
import com.machpay.api.entity.User;
import com.machpay.api.user.UserService;
import com.machpay.api.user.auth.dto.SignInRequest;
import com.machpay.api.util.HttpServletRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("authenticationFailureHandler")
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private Messages messages;

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response,
                                        final AuthenticationException exception) throws IOException, ServletException {
        String error = exception.getMessage();
        AuthProvider provider = getProvider(request);

        if (error.equalsIgnoreCase("Bad credentials")) {
            if (provider.equals(AuthProvider.FACEBOOK) || provider.equals(AuthProvider.GOOGLE)) {
                error = messages.get("user.account.pleaseProceedWithSocialLogin", provider.getProvider());
            } else {
                Integer loginAttempts = getRemainingLoginAttempts(request);
                error = (loginAttempts > 0)
                        ? messages.get("user.account.badCredentials", loginAttempts.toString())
                        : messages.get("user.account.locked");
            }
        }

        if (error.contains("user not found with email")) {
            error = messages.get("user.account.userNotFoundWithGivenEmail");
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, error);
    }

    private AuthProvider getProvider(HttpServletRequest request) {
        byte[] bytes = HttpServletRequestUtils.getRequestReaderByte(request);
        String email = HttpServletRequestUtils.getAuthRequest(bytes).getEmail();
        User user = userService.findByEmail(email);

        return user.getProvider();
    }

    private Integer getRemainingLoginAttempts(HttpServletRequest request) {
        byte[] bytes = HttpServletRequestUtils.getRequestReaderByte(request);
        SignInRequest authRequest = HttpServletRequestUtils.getAuthRequest(bytes);

        return Constants.MAX_LOGIN_LIMIT - userService.getLoginAttempts(authRequest.getEmail());
    }
}
