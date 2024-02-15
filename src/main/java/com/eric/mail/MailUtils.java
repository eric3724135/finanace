package com.eric.mail;

import lombok.extern.slf4j.Slf4j;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

@Slf4j
public class MailUtils {
    private static Properties mailServerProperties = null;


    public static void generateAndSendEmail(String[] to, String subject, String msg, String filename, ByteArrayOutputStream bos) throws MessagingException {

        if (mailServerProperties == null) {
            // Step1
            log.info("\n 1st ===> setup Mail Server Properties..");
            mailServerProperties = System.getProperties();
            mailServerProperties.put("mail.smtp.port", "587");
            mailServerProperties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            mailServerProperties.put("mail.smtp.auth", "true");
            mailServerProperties.put("mail.smtp.starttls.enable", "true");
            log.info("Mail Server Properties have been setup successfully..");

        }
        // Step2
        log.info("\n\n 2nd ===> get Mail Session..");
        Session getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        MimeMessage generateMailMessage = new MimeMessage(getMailSession);
        for (String s : to) generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(s));
        //generateMailMessage.addRecipient(Message.RecipientType.CC, new InternetAddress("test2@crunchify.com"));
        generateMailMessage.setSubject(subject);
        generateMailMessage.setContent(msg, "text/html; charset=UTF-8");
        log.info("Mail Session has been created successfully..");

        //TODO finish add attachment
        if (bos != null) {
            Multipart multipart = new MimeMultipart();
            DataSource source = new ByteArrayDataSource(bos.toByteArray(), "application/vnd.ms-excel");
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);

            generateMailMessage.setContent(multipart);
        }

        // Step3
        log.info("\n\n 3rd ===> Get Session and Send mail");
        Transport transport = getMailSession.getTransport("smtp");

        // Enter your correct gmail UserID and Password
        // if you have 2FA enabled then provide App Specific Password
        transport.connect("smtp.gmail.com", 587, "sender@gmail.com", "");
        transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
        transport.close();
    }

}
