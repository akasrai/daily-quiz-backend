package com.machpay.api.user.verification;

import com.machpay.api.common.Constants;
import com.machpay.api.common.enums.ApplicationEnvironment;
import com.machpay.api.common.enums.ContactType;
import com.machpay.api.common.enums.ContactVerificationStatus;
import com.machpay.api.common.enums.ServerSentEvent;
import com.machpay.api.common.exception.BadRequestException;
import com.machpay.api.common.exception.ResourceNotFoundException;
import com.machpay.api.common.exception.TokenExpiredException;
import com.machpay.api.entity.Member;
import com.machpay.api.entity.ContactVerification;
import com.machpay.api.entity.User;
import com.machpay.api.redis.AuthTokenService;
import com.machpay.api.serversentevent.ServerSentEventService;
import com.machpay.api.twilio.TwilioSMSService;
import com.machpay.api.twilio.VerificationCodeGenerator;
import com.machpay.api.user.UserService;
import com.machpay.api.user.member.MemberMailService;
import com.machpay.api.user.member.MemberRepository;
import com.machpay.api.user.member.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ContactVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(ContactVerificationService.class);

    @Autowired
    private Environment environment;

    @Autowired
    private MemberMailService senderMailService;

    @Autowired
    private ContactVerificationRepository contactVerificationRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TwilioSMSService twilioSmsService;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private ServerSentEventService serverSentEventService;

    public void createDeviceVerification(Long senderId, ContactType contactType) {
        Member sender = memberService.findById(senderId);

        if (!isDeviceVerified(sender, contactType))
            checkVerificationToken(sender, contactType);

        else
            throw new BadRequestException("Your " + contactType.name() + " is already verified.");
    }

    private boolean isDeviceVerified(Member sender, ContactType contactType) {
        return ContactType.PHONE.equals(contactType)
                ? sender.isPhoneNumberVerified()
                : sender.isEmailVerified();

    }

    private void checkVerificationToken(Member sender, ContactType contactType) {
        ContactVerification contactVerification = contactVerificationRepository.existsByUserAndType(sender, contactType)
                ? checkTwoFAVerificationResendAttempts(sender, contactType)
                : createVerificationToken(sender, contactType);

        sendVerificationMessage(sender, contactType, contactVerification.getToken());
    }

    private ContactVerification checkTwoFAVerificationResendAttempts(Member sender, ContactType contactType) {
        ContactVerification contactVerification = contactVerificationRepository.findByUserIdAndType(sender.getId(),
                contactType);

        if (contactVerification.getResendAttempt() >= Constants.RESEND_VERIFICATION_CODE_LIMIT) {
            lockAndInvalidateToken(contactVerification.getUser());

            throw new BadRequestException(Constants.RESEND_VERIFICATION_CODE_LIMIT_EXCEEDED);
        } else {
            return updateVerificationToken(contactVerification, contactType);
        }
    }

    @Transactional
    public ContactVerification createVerificationToken(Member sender, ContactType contactType) {
        ContactVerification contactVerification = new ContactVerification();

        contactVerification.setUser(sender);
        contactVerification.setType(contactType);
        contactVerification.setToken(generateVerificationCode(contactType));
        contactVerification.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        contactVerification.setStatus(ContactVerificationStatus.PENDING);
        contactVerification.setVerificationAttempt(0);

        return contactVerificationRepository.save(contactVerification);
    }

    @Transactional
    public ContactVerification updateVerificationToken(ContactVerification contactVerification, ContactType contactType) {
        contactVerification.setToken(generateVerificationCode(contactType));
        contactVerification.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        contactVerification.setVerificationAttempt(0);
        contactVerification.setResendAttempt(contactVerification.getResendAttempt() + 1);

        return contactVerificationRepository.save(contactVerification);
    }

    private void lockAndInvalidateToken(User user) {
        userService.lock(user.getEmail(),
                Constants.RESEND_VERIFICATION_CODE_LIMIT_EXCEEDED);
        authTokenService.deleteAuthTokenByUserId(user.getId());
    }

    @Transactional
    public void resetResendAttempts(Member sender, ContactType contactType) {
        ContactVerification contactVerification =
                contactVerificationRepository.findByUserAndType(sender, contactType).orElseThrow(() -> new ResourceNotFoundException(
                        "TwoFaVerification", "User", sender));
        contactVerification.setResendAttempt(0);
        contactVerificationRepository.save(contactVerification);
    }

    private String generateVerificationCode(ContactType contactType) {
        if (ContactType.PHONE.equals(contactType)) {
            if (environment.acceptsProfiles(Profiles.of(ApplicationEnvironment.PROD.getEnvironment()))) {
                return VerificationCodeGenerator.generate();
            }

            return Constants.DEFAULT_PHONE_VERIFICATION_CODE;
        }


        return VerificationCodeGenerator.generate();
    }

    private void sendVerificationMessage(Member sender, ContactType contactType, String token) {
        if (ContactType.PHONE.equals(contactType)) {
            sendPhoneVerificationCode(sender, token);
        } else {
            sendVerificationEmail(sender, token);
        }
    }

    private void sendVerificationEmail(Member sender, String token) {
        senderMailService.sendVerificationMail(sender, token);
    }

    private void sendPhoneVerificationCode(Member sender, String token) {
        if (environment.acceptsProfiles(Profiles.of(ApplicationEnvironment.PROD.getEnvironment()))) {
            twilioSmsService.sendVerificationCode(sender.getPhoneNumber(), token.concat(" is your verification code."));
        }
    }

    @Transactional
    public void verifyToken(Long id, String token, ContactType contactType) {
        ContactVerification contactVerification = contactVerificationRepository.findByUserIdAndType(id, contactType);
        Member member = memberService.findById(id);

        if (contactVerification == null) {
            throw new BadRequestException("User has no verification data");
        }

        if (token.isEmpty()) {
            throw new BadRequestException("Verification token is empty.");
        }

        if (contactVerification.getExpiryDate().isBefore(LocalDateTime.now())) {
            logger.info("Verification token: [{}] expired with expiry date [{}] before [{}]", token,
                    contactVerification.getExpiryDate(), LocalDateTime.now());
            throw new TokenExpiredException("Verification token is expired.");
        }

        switch (contactType) {
            case PHONE:
                verifyPhone(member, contactVerification, token);
                serverSentEventService.emitEvent(ServerSentEvent.PHONE_NUMBER_VERIFIED, member.getReferenceId());
                break;

            case EMAIL:
                verifyEmail(contactVerification, token);
                UUID referenceId = memberService.findById(contactVerification.getUser().getId()).getReferenceId();
                serverSentEventService.emitEvent(ServerSentEvent.EMAIL_VERIFIED, referenceId);
                break;
        }
    }

    @Transactional
    public void verifyEmail(ContactVerification contactVerification, String token) {
        if (contactVerification.getToken().equals(token)) {
            contactVerification.setVerifiedAt(LocalDateTime.now());
            contactVerification.setStatus(ContactVerificationStatus.VERIFIED);
            contactVerification.getUser().setEmailVerified(true);
            contactVerification.setExpiryDate(LocalDateTime.now());
            contactVerificationRepository.save(contactVerification);
        } else {
            contactVerification.setVerificationAttempt(contactVerification.getVerificationAttempt() + 1);
            contactVerificationRepository.save(contactVerification);

            throw new BadRequestException("Invalid verification token.");
        }
    }

    @Transactional
    public void verifyPhone(Member sender, ContactVerification contactVerification, String token) {
        if (contactVerification.getToken().equals(token)) {
            contactVerification.setVerifiedAt(LocalDateTime.now());
            contactVerification.setStatus(ContactVerificationStatus.VERIFIED);
            contactVerification.setVerificationAttempt(contactVerification.getVerificationAttempt() + 1);
            contactVerificationRepository.save(contactVerification);

            sender.setPhoneNumberVerified(true);
            memberRepository.save(sender);
        } else {
            contactVerification.setVerificationAttempt(contactVerification.getVerificationAttempt() + 1);
            contactVerificationRepository.save(contactVerification);

            throw new BadRequestException("Verification code does not match.");
        }
    }
}