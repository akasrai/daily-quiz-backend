package com.machpay.api.user.auth;

import com.machpay.api.common.enums.ContactType;
import com.machpay.api.common.enums.RoleType;
import com.machpay.api.common.exception.BadRequestException;
import com.machpay.api.entity.Member;
import com.machpay.api.entity.Role;
import com.machpay.api.entity.User;
import com.machpay.api.redis.AuthToken;
import com.machpay.api.redis.AuthTokenService;
import com.machpay.api.security.TokenProvider;
import com.machpay.api.security.UserPrincipal;
import com.machpay.api.user.UserService;
import com.machpay.api.user.admin.AdminService;
import com.machpay.api.user.auth.dto.AccessTokenRequest;
import com.machpay.api.user.auth.dto.AccessTokenResponse;
import com.machpay.api.user.auth.dto.AuthResponse;
import com.machpay.api.user.auth.dto.CurrentUserResponse;
import com.machpay.api.user.auth.dto.SignInRequest;
import com.machpay.api.user.auth.dto.SignUpRequest;
import com.machpay.api.user.member.MemberService;
import com.machpay.api.user.role.RoleService;
import com.machpay.api.user.verification.ContactVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@Service
public class AuthService {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ContactVerificationService contactVerificationService;

    private Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthResponse signIn(SignInRequest signInRequest) {
        String token = authenticate(signInRequest.getEmail(), signInRequest.getPassword());
        User user = userService.findByEmail(signInRequest.getEmail());

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

    public void signOut(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null) {
            String referenceToken = authHeader.replace("Bearer", "").trim();
            authTokenService.deleteAuthTokenByReferenceToken(referenceToken);
        }
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

    public AccessTokenResponse refreshAccessToken(AccessTokenRequest accessTokenRequest) {
        AuthToken authToken = authTokenService.getAuthToken(accessTokenRequest.getReferenceToken());
        String referenceToken = tokenProvider.createAccessToken(authToken.getUserId());

        logger.info("Removing expired pair of auth tokens from redis with referenceId:{}",
                accessTokenRequest.getReferenceToken());
        authTokenService.deleteAuthTokenByReferenceToken(accessTokenRequest.getReferenceToken());

        return new AccessTokenResponse(referenceToken);
    }

    public CurrentUserResponse getCurrentUser(UserPrincipal userPrincipal) {
        Role admin = roleService.findByName(RoleType.ROLE_ADMIN);

        if (userService.findByEmail(userPrincipal.getEmail()).isRole(admin))
            return adminService.getCurrentAdmin(userPrincipal.getEmail());

        return memberService.getCurrentMember(userPrincipal.getEmail());
    }
}
