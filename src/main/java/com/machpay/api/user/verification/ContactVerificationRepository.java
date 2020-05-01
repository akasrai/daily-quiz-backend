package com.machpay.api.user.verification;

import com.machpay.api.common.enums.ContactType;
import com.machpay.api.entity.ContactVerification;
import com.machpay.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ContactVerificationRepository extends JpaRepository<ContactVerification, String> {

    ContactVerification findByToken(String token);

    ContactVerification findByUserIdAndType(Long id, Enum type);

    Boolean existsByUserAndType(User user, ContactType contactType);

    Optional<ContactVerification> findByUserAndType(User user, ContactType contactType);
}