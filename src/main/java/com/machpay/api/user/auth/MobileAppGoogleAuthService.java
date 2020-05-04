package com.machpay.api.user.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.machpay.api.common.enums.AuthProvider;
import com.machpay.api.common.enums.RoleType;
import com.machpay.api.common.exception.BadRequestException;
import com.machpay.api.config.MobileAppConfig;
import com.machpay.api.entity.Member;
import com.machpay.api.entity.Role;
import com.machpay.api.entity.User;
import com.machpay.api.security.TokenProvider;
import com.machpay.api.security.UserPrincipal;
import com.machpay.api.user.auth.dto.AuthResponse;
import com.machpay.api.user.member.MemberService;
import com.machpay.api.user.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MobileAppGoogleAuthService {

    private String googleSecretId;

    @Autowired
    private AuthService authService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    MobileAppGoogleAuthService(MobileAppConfig mobileAppConfig) {
        this.googleSecretId = mobileAppConfig.getGoogleClientId();
    }

    private GoogleIdTokenVerifier getVerifier() {
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            return new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
                    .setAudience(Collections.singletonList(this.googleSecretId))
                    .build();
        } catch (IOException | GeneralSecurityException gse) {
            throw new BadRequestException("Login failed");
        }
    }

    private GoogleIdToken getGoogleIdToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = getVerifier();

            return verifier.verify(idToken);
        } catch (IOException | GeneralSecurityException gse) {
            throw new BadRequestException("Login failed");
        }
    }

    public AuthResponse signIn(String idToken) {
        GoogleIdToken googleIdToken = getGoogleIdToken(idToken);
        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        Member member = createMember(payload);
        String token = authenticate(member);

        return authService.buildAuthResponse(member, token);
    }

    private Member createMember(GoogleIdToken.Payload payload) {
        if (!memberService.existsByEmail(payload.getEmail())) {
            Member member = new Member();
            Role roleUser = roleService.findByName(RoleType.ROLE_MEMBER);

            member.setEmail(payload.getEmail());
            member.setProvider(AuthProvider.GOOGLE);
            member.setPhoto((String) payload.get("picture"));
            member.setProviderId((String) payload.get("sub"));
            member.setEmailVerified(payload.getEmailVerified());
            member.setFirstName((String) payload.get("given_name"));
            member.setLastName((String) payload.get("family_name"));
            member.setMiddleName((String) payload.get("middle_name"));
            member.setReferenceId(memberService.generateReferenceId());
            member.setRoles(new ArrayList<>(Collections.singletonList(roleUser)));

            return memberService.save(member);
        }

        return memberService.findByEmail(payload.getEmail());
    }

    public String authenticate(User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                getAuthorities(user));
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenProvider.createAccessToken(userPrincipal.getId());
    }

    private List<GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        List<String> privileges = UserPrincipal.getPrivileges(user.getRoles());
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }

        return authorities;
    }
}
