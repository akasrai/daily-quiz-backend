package com.machpay.api.user.privilege;

import com.machpay.api.common.enums.PrivilegeType;
import com.machpay.api.entity.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {

    Privilege findByName(PrivilegeType name);
}
