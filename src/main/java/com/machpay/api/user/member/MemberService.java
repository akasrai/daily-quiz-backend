package com.machpay.api.user.member;

import com.machpay.api.common.enums.AuthProvider;
import com.machpay.api.common.enums.ContactType;
import com.machpay.api.common.enums.RoleType;
import com.machpay.api.common.exception.ResourceNotFoundException;
import com.machpay.api.entity.Member;
import com.machpay.api.entity.Role;
import com.machpay.api.user.auth.dto.Oauth2SignUpRequest;
import com.machpay.api.user.auth.dto.SignUpRequest;
import com.machpay.api.user.member.dto.MemberResponse;
import com.machpay.api.user.role.RoleService;
import com.machpay.api.user.verification.ContactVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class MemberService {
    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ContactVerificationService contactVerificationService;

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));
    }

    public Member findByReferenceId(UUID referenceId) {
        return memberRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", referenceId));
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", email));
    }

    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    public boolean isPhoneDuplicate(String phoneNumber) {
        return memberRepository.existsByPhoneNumber(phoneNumber);
    }

    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    public Member create(SignUpRequest signUpRequest) {
        Role roleUser = roleService.findByName(RoleType.ROLE_MEMBER);
        Member member = memberMapper.toMember(signUpRequest);

        member.setProvider(AuthProvider.SYSTEM);
        member.setReferenceId(generateReferenceId());
        member.setRoles(new ArrayList<>(Collections.singletonList(roleUser)));
        member.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        return save(member);
    }

    public Member save(Member sender) {
        return memberRepository.save(sender);
    }

    public MemberResponse getCurrentMember(String email) {
        Member member = findByEmail(email);

        return memberMapper.toMemberResponse(member);
    }

    public List<MemberResponse> getLockedSenders() {
        List<Member> senders = memberRepository.findAllByLocked(true);

        return memberMapper.toMemberResponseList(senders);
    }

    @Transactional
    public void unLock(UUID referenceId) {
        Member member = findByReferenceId(referenceId);

        if (!member.isEmailVerified())
            contactVerificationService.resetResendAttempts(member, ContactType.EMAIL);

        if (!member.isPhoneNumberVerified())
            contactVerificationService.resetResendAttempts(member, ContactType.PHONE);

        member.setLoginAttempts(0);
        member.setLocked(false);
        memberRepository.save(member);
    }

    public UUID generateReferenceId() {
        return UUID.randomUUID();
    }
}