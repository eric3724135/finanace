package com.eric.mail;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("mail")
@Data
public class MailConfig {

    private String account;

    private String password;

    private String address;

    private String[] addressArr;


    public String[] getAddressArr() {
        if (addressArr == null) {
            addressArr = address.split(";");
        }
        return addressArr;
    }

    private String getAddress() {
        return address;
    }
}
