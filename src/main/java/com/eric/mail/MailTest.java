package com.eric.mail;

import javax.mail.MessagingException;

public class MailTest {
    public static void main(String[] args) throws MessagingException {
        String[] to = new String[]{"sender@gmail.com"};

        MailUtils.generateAndSendEmail(to,"test","test","test.txt",null);
    }
}
