package com.machpay.api.twilio;

import com.machpay.api.common.exception.BadRequestException;
import com.machpay.api.config.TwilioConfig;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class TwilioSMSService {
    private Logger logger = LoggerFactory.getLogger(TwilioSMSService.class);

    private String fromPhoneNumber;

    @Autowired
    TwilioSMSService(TwilioConfig twilioConfig) {
        fromPhoneNumber = twilioConfig.getFromPhoneNumber();
        Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
    }

    @Async
    public void sendVerificationCode(String toPhoneNumber, String smsContent) {
        if (!sendSMS(toPhoneNumber, smsContent)) {
            throw new BadRequestException("Failed to send verification code to your phone number.");
        }
    }

    public Boolean sendSMS(String toPhoneNumber, String smsContent) {
        Message message = Message.creator(
                new PhoneNumber(toPhoneNumber),
                new PhoneNumber(fromPhoneNumber),
                smsContent).create();
        logger.debug("Twilio SMS Service: {}", message);

        return true;
    }
}
