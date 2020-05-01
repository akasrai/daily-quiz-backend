package com.machpay.api.mail;

import com.machpay.api.config.MailConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

@Service
public class MailService {

    private final MailConfig mailConfig;

    private Logger logger = LoggerFactory.getLogger(MailService.class);

    @Autowired
    MailService(MailConfig mailConfig) {
        this.mailConfig = mailConfig;
    }

    public boolean sendMail(String toEmail, String subject, String body) {
        try {
            Session session = getSession(getProperties());
            MimeMessage msg = getMimeMessage(session, subject, toEmail);
            msg.setContent(body, "text/html");
            transport(session, msg);

            return true;
        } catch (Exception ex) {
            logger.error("Error while sending mail to email: {}", toEmail, ex);
        }

        return false;
    }

    public boolean sendMail(String toEmail, String subject, String body, String attachment) {
        try {
            Session session = getSession(getProperties());
            MimeMessage msg = getMimeMessage(session, subject, toEmail);
            msg.setContent(getMultiPart(body, attachment));
            transport(session, msg);

            return true;
        } catch (Exception ex) {
            logger.error("Error while sending mail to email: {}", toEmail, ex);
        }

        return false;
    }

    private Properties getProperties() {
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", mailConfig.getPort());
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");

        return props;
    }

    private Session getSession(Properties props) {
        Session session = Session.getDefaultInstance(props);
        session.setDebug(true);

        return session;
    }

    private MimeMessage getMimeMessage(Session session, String subject, String toEmail) throws MessagingException {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(mailConfig.getUsername());
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        msg.setSubject(subject);

        return msg;
    }

    private Multipart getMultiPart(String body, String attachment) throws MessagingException
            , MalformedURLException {
        Multipart multipart = new MimeMultipart();
        BodyPart messageBodyPart = new MimeBodyPart();

        messageBodyPart.setContent(body, "text/html");
        multipart.addBodyPart(messageBodyPart);

        URL url = new URL(attachment);
        BodyPart attachmentFile = new MimeBodyPart();
        attachmentFile.setDataHandler(new DataHandler(url));
        attachmentFile.setDisposition(Part.ATTACHMENT);
        attachmentFile.setFileName("filename");
        multipart.addBodyPart(attachmentFile);

        return multipart;
    }

    private void transport(Session session, MimeMessage msg) throws MessagingException {
        Transport transport = session.getTransport();
        transport.connect(mailConfig.getHost(), mailConfig.getUsername(), mailConfig.getPassword());
        transport.sendMessage(msg, msg.getAllRecipients());
    }
}
