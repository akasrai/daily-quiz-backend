package com.machpay.api.config;

import com.machpay.api.common.Constants;
import com.machpay.api.common.enums.AuthProvider;
import com.machpay.api.common.enums.PrivilegeType;
import com.machpay.api.common.enums.RoleType;
import com.machpay.api.entity.Admin;
import com.machpay.api.entity.Privilege;
import com.machpay.api.entity.QuizSeason;
import com.machpay.api.entity.Role;
import com.machpay.api.quiz.repository.QuizSeasonRepository;
import com.machpay.api.user.admin.AdminRepository;
import com.machpay.api.user.privilege.PrivilegeRepository;
import com.machpay.api.user.role.RoleRepository;
import com.machpay.api.user.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private QuizSeasonRepository quizSeasonRepository;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup)
            return;

        Privilege readPrivilege = createPrivilegeIfNotFound(PrivilegeType.ROLE_READ);
        Privilege writePrivilege = createPrivilegeIfNotFound(PrivilegeType.ROLE_WRITE);

        List<Privilege> readWritePrivilege = Arrays.asList(readPrivilege, writePrivilege);

        createRoleIfNotFound(RoleType.ROLE_ADMIN, readWritePrivilege);
        createRoleIfNotFound(RoleType.ROLE_MEMBER, readWritePrivilege);

        Role adminRole = roleService.findByName(RoleType.ROLE_ADMIN);
        createAdminIfNotFound(adminRole);
        createInitialQuizSeason();

        alreadySetup = true;
    }

    @Transactional
    public Admin createAdminIfNotFound(Role adminRole) {
        Admin admin = new Admin();

        if (!adminRepository.existsByEmail("admin@admin.com")) {
            admin.setEmail("admin@admin.com");
            admin.setFirstName("Super");
            admin.setLastName("Admin");
            admin.setProvider(AuthProvider.SYSTEM);
            admin.setPassword(passwordEncoder.encode("3MtkS-#Vk?2?Ek3B"));
            admin.setRoles(new ArrayList<>(Collections.singletonList(adminRole)));
            adminRepository.save(admin);
        }

        return admin;
    }

    @Transactional
    public Privilege createPrivilegeIfNotFound(PrivilegeType name) {
        Privilege privilege = privilegeRepository.findByName(name);

        if (privilege == null) {
            privilege = new Privilege(name);
            privilegeRepository.save(privilege);
        }

        return privilege;
    }

    @Transactional
    public void createRoleIfNotFound(RoleType name, Collection<Privilege> privileges) {
        if (!roleService.existsByName(name)) {
            Role role = new Role(name);
            role.setPrivileges(privileges);
            roleRepository.save(role);
        }
    }

    @Transactional
    public void createInitialQuizSeason() {
        if (!quizSeasonRepository.existsByTitle(Constants.INITIAL_QUIZ_SEASON)) {
            QuizSeason quizSeason = new QuizSeason();
            quizSeason.setTitle(Constants.INITIAL_QUIZ_SEASON);
            quizSeason.setActive(true);

            quizSeasonRepository.save(quizSeason);
        }
    }
}