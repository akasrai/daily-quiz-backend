package com.machpay.api.user.admin;

import com.machpay.api.common.enums.RoleType;
import com.machpay.api.common.exception.ResourceNotFoundException;
import com.machpay.api.entity.Admin;
import com.machpay.api.user.auth.dto.CurrentUserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminMapper adminMapper;

    public Admin findByEmail(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("admin", "id", email));
    }

    public boolean existsByEmail(String email) {
        return adminRepository.existsByEmail(email);
    }

    public CurrentUserResponse getCurrentAdmin(String email) {
        Admin admin = findByEmail(email);

        CurrentUserResponse currentUserResponse = adminMapper.toCurrentUserResponse(admin);
        currentUserResponse.setRoles(admin.getRoles().stream()
                .map(role -> RoleType.valueOf(role.getName().toString()).toString().split("_")[1])
                .collect(Collectors.toList()));

        return currentUserResponse;
    }
}