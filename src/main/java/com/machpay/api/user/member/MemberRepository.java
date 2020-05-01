package com.machpay.api.user.member;

import com.machpay.api.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByReferenceId(UUID referenceId);

    Boolean existsByEmail(String email);

    Boolean existsByPhoneNumber(String phoneNumber);

    List<Member> findAllByLocked(boolean isLocked);
}