package com.machpay.api.security.oauth2;

import com.machpay.api.common.enums.AuthProvider;
import com.machpay.api.common.enums.RoleType;
import com.machpay.api.common.exception.OAuth2AuthenticationProcessingException;
import com.machpay.api.entity.Member;
import com.machpay.api.entity.Role;
import com.machpay.api.security.UserPrincipal;
import com.machpay.api.security.oauth2.user.OAuth2UserInfo;
import com.machpay.api.security.oauth2.user.OAuth2UserInfoFactory;
import com.machpay.api.user.member.MemberRepository;
import com.machpay.api.user.member.MemberService;
import com.machpay.api.user.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private RoleService roleService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo =
                OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId()
                        , oAuth2User.getAttributes());
        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<Member> senderOptional = memberRepository.findByEmail(oAuth2UserInfo.getEmail());
        Member sender;
        if (senderOptional.isPresent()) {
            sender = senderOptional.get();
            if (!sender.getProvider().equals(AuthProvider.get(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException(String.format("Looks like you're signed up with %s " +
                        "account. Please use your %s account to login.", sender.getProvider(), sender.getProvider()));
            }
            sender = updateExistingMember(sender, oAuth2UserInfo);
        } else {
            sender = registerMember(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(sender, oAuth2User.getAttributes());
    }

    private Member registerMember(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        Member member = new Member();
        Role roleUser = roleService.findByName(RoleType.ROLE_GUEST);

        member.setProvider(AuthProvider.get(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        member.setEmailVerified(true);
        member.setEmail(oAuth2UserInfo.getEmail());
        member.setProviderId(oAuth2UserInfo.getId());
        member.setImageUrl(oAuth2UserInfo.getImageUrl());
        member.setLastName(oAuth2UserInfo.getLastName());
        member.setFirstName(oAuth2UserInfo.getFirstName());
        member.setMiddleName(oAuth2UserInfo.getMiddleName());
        member.setRoles(new ArrayList<>(Collections.singletonList(roleUser)));

        return memberService.save(member);
    }

    private Member updateExistingMember(Member existingMember, OAuth2UserInfo oAuth2UserInfo) {
        existingMember.setImageUrl(oAuth2UserInfo.getImageUrl());
        existingMember.setLastName(oAuth2UserInfo.getLastName());
        existingMember.setFirstName(oAuth2UserInfo.getFirstName());
        existingMember.setMiddleName(oAuth2UserInfo.getMiddleName());

        return memberService.save(existingMember);
    }

}