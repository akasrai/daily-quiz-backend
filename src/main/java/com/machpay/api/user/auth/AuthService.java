package com.machpay.api.user.auth;

import com.machpay.api.common.enums.ContactType;
import com.machpay.api.common.enums.RoleType;
import com.machpay.api.common.exception.BadRequestException;
import com.machpay.api.entity.Member;
import com.machpay.api.entity.User;
import com.machpay.api.redis.AuthToken;
import com.machpay.api.redis.AuthTokenService;
import com.machpay.api.security.TokenProvider;
import com.machpay.api.security.UserPrincipal;
import com.machpay.api.user.UserService;
import com.machpay.api.user.auth.dto.AccessTokenRequest;
import com.machpay.api.user.auth.dto.AccessTokenResponse;
import com.machpay.api.user.auth.dto.AuthResponse;
import com.machpay.api.user.auth.dto.LoginRequest;
import com.machpay.api.user.auth.dto.Oauth2SignupRequest;
import com.machpay.api.user.auth.dto.SignUpRequest;
import com.machpay.api.user.member.MemberMapper;
import com.machpay.api.user.member.MemberService;
import com.machpay.api.user.member.dto.MemberResponse;
import com.machpay.api.user.verification.ContactVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {
    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ContactVerificationService contactVerificationService;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private UserService userService;

    private Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthResponse login(LoginRequest loginRequest) {
        String token = authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        User user = userService.findByEmail(loginRequest.getEmail());

        return buildAuthResponse(user, token);
    }

    public AuthResponse signUp(SignUpRequest signUpRequest) {

        if (memberService.isEmailDuplicate(signUpRequest.getEmail())) {
            throw new BadRequestException("Member with given email already exists.");
        }

        Member member = memberService.create(signUpRequest);
        String token = authenticate(signUpRequest.getEmail(), signUpRequest.getPassword());

        contactVerificationService.createDeviceVerification(member.getId(), ContactType.EMAIL);
        contactVerificationService.createDeviceVerification(member.getId(), ContactType.PHONE);

        return buildAuthResponse(member, token);
    }

    private String authenticate(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return tokenProvider.createAccessToken(userPrincipal.getId());
    }

    public AuthResponse buildAuthResponse(User user, String token) {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setRoles(user.getRoles().stream()
                .map(role -> RoleType.valueOf(role.getName().toString()).toString().split("_")[1])
                .collect(Collectors.toList()));

        return authResponse;
    }

    public MemberResponse getGuestInfo(Long senderId) {
        Member member = memberService.findById(senderId);

        return memberMapper.toMemberResponse(member);
    }

    public MemberResponse completeOauth2SignUp(Oauth2SignupRequest oauth2SignupRequest,
                                               UserPrincipal userPrincipal) {
        if (memberService.isPhoneDuplicate(oauth2SignupRequest.getPhoneNumber())) {
            throw new BadRequestException("Phone number already in use.");
        }

        Member member = memberService.findById(userPrincipal.getId());
        member = memberService.updateOauth2Member(oauth2SignupRequest, member);
        grantNewAuthentication(member);
        contactVerificationService.createDeviceVerification(member.getId(), ContactType.PHONE);

        return memberMapper.toMemberResponse(member);
    }

    private void grantNewAuthentication(Member sender) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authentication.getAuthorities());
        List<String> privileges = UserPrincipal.getPrivileges(sender.getRoles());

        for (String privilege : privileges) {
            updatedAuthorities.add(new SimpleGrantedAuthority(privilege));
        }

        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),
                authentication.getCredentials(),
                updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
    }

    public AccessTokenResponse refreshAccessToken(AccessTokenRequest accessTokenRequest) {
        AuthToken authToken = authTokenService.getAuthToken(accessTokenRequest.getReferenceToken());
        String referenceToken = tokenProvider.createAccessToken(authToken.getUserId());

        logger.info("Removing expired pair of auth tokens from redis with referenceId:{}",
                accessTokenRequest.getReferenceToken());
        authTokenService.deleteAuthTokenByReferenceToken(accessTokenRequest.getReferenceToken());

        return new AccessTokenResponse(referenceToken);
    }
}
