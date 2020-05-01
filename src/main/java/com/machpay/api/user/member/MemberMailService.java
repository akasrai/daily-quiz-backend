package com.machpay.api.user.member;

import com.machpay.api.common.Constants;
import com.machpay.api.config.MailConfig;
import com.machpay.api.entity.Member;
import com.machpay.api.mail.MailService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class MemberMailService {

    private static final Logger logger = LoggerFactory.getLogger(MemberMailService.class);
    private final Configuration template;
    private final MailConfig mailConfig;
    @Autowired
    private MailService mailService;

    @Autowired
    MemberMailService(MailConfig mailConfig, Configuration template) {
        this.mailConfig = mailConfig;
        this.template = template;
    }

    @Async
    public void sendVerificationMail(Member sender, String verificationCode) {
        try {
            Template t = template.getTemplate("email-verification.ftl");
            Map<String, String> map = getTemplateVariable(sender);
            map.put("VERIFICATION_CODE", verificationCode);
            String body = FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
            mailService.sendMail(sender.getEmail(), Constants.VERIFY_EMAIL_ID, body);
        } catch (Exception ex) {
            logger.error("Error while sending verification mail to member id: {}", sender.getEmail(), ex);
        }
    }

    @Async
    public void sendMail(Member sender, String subject, String emailTemplate) {
        try {
            String templateName = emailTemplate;
            if (!templateName.isEmpty()) {
                Template t = template.getTemplate(templateName);
                String body = FreeMarkerTemplateUtils.processTemplateIntoString(t, getTemplateVariable(sender));
                mailService.sendMail(sender.getEmail(), subject, body);
            } else {
                logger.warn("No template found to send an email.");
            }

        } catch (Exception ex) {
            logger.error("Error while sending email", ex);
        }
    }

    private Map<String, String> getTemplateVariable(Member sender) {
        Map<String, String> map = new HashMap<>();
        map.put("USER_NAME", sender.getFullName());
        map.put("BASE_URL", mailConfig.getBaseUrl());

        return map;
    }
}
