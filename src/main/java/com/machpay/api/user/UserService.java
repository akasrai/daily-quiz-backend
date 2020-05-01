package com.machpay.api.user;

import com.machpay.api.common.exception.ResourceNotFoundException;
import com.machpay.api.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("user", "email",
                email));
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("user", "id", id));
    }

    public void resetPassword(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public Integer getLoginAttempts(String email) {
        return userRepository.getLoginAttempts(email);
    }

    @Transactional
    public void lock(String email, String reason) {
        User user = findByEmail(email);
        user.setLocked(true);
        user.setLockedReason(reason);
        save(user);
    }
}
