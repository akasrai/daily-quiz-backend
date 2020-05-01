package com.machpay.api.user.role;

import com.machpay.api.common.enums.RoleType;
import com.machpay.api.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleType name);

    Boolean existsByName(RoleType name);
}
