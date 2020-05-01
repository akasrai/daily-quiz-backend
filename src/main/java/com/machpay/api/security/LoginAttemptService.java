package com.machpay.api.security;

import com.machpay.api.common.Constants;
import com.machpay.api.common.enums.AuthProvider;
import com.machpay.api.entity.User;
import com.machpay.api.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    @Autowired
    private UserService userService;

    public void loginSucceeded(String email) {
        User user = userService.findByEmail(email);
        user.setLoginAttempts(0);
        userService.save(user);
    }

    public void loginFailed(String email) {
        User user = userService.findByEmail(email);
        if (!AuthProvider.FACEBOOK.equals(user.getProvider()) && !AuthProvider.GOOGLE.equals(user.getProvider())) {
            user.setLoginAttempts(user.getLoginAttempts() + 1);
            userService.save(user);
            lockIfUserHasMaxLoginAttempts(email);
        }
    }

    public boolean isLocked(String email) {
        User user = userService.findByEmail(email);
        return user.isLocked();
    }

    private void lockIfUserHasMaxLoginAttempts(String email) {
        User user = userService.findByEmail(email);
        if (user.getLoginAttempts() >= Constants.MAX_LOGIN_LIMIT) {
            userService.lock(email, Constants.MAX_LOGIN_LIMIT_EXCEEDED);
        }
    }
}
