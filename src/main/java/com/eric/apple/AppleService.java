package com.eric.apple;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

@Slf4j
@Service
public class AppleService {

    private static final String URL_TEMPLATE = "https://www.apple.com/tw/shop/fulfillment-messages?pl=true&mts.0=compact&parts.0=%s&searchNearby=true&store=R713";

    @Autowired
    private HookConfig hookConfig;


    public void start() {
        Map<String, String> prodMap = new HashMap<>();
        prodMap.put("MYT33TA/A", "test");
//        prodMap.put("MYNJ3ZP/A","16 pro 256 白");
//        prodMap.put("MYNK3ZP/A","16 pro 256 沙");
//        prodMap.put("MYWX3ZP/A","16 max 256 沙");
//        prodMap.put("MYWW3ZP/A","15 max 256 白");
        try {
            this.appleGroupProductParser(prodMap);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void appleGroupProductParser(Map<String, String> params) throws InterruptedException {
        List<String> prodKeys = new ArrayList<>();
        for (Map.Entry<String, String> prod : params.entrySet()) {
            prodKeys.add(prod.getKey());
        }
        AppleGroupPickUpParser pickUpParser = new AppleGroupPickUpParser(params, hookConfig.getMake(), hookConfig.getBark());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        while (true) {
            log.info("查詢開始 {}", new Date());
            Calendar current = Calendar.getInstance();
            if (current.after(cal)) {
                String url = String.format(hookConfig.getMake(), " APPLE 服務還活著！！");
                try {
                    Jsoup.connect(url)
                            .sslSocketFactory(socketFactory())
                            .ignoreContentType(true).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cal.add(Calendar.HOUR_OF_DAY, 1);
                //Thread.sleep(10000);
            }
            pickUpParser.getResult();
            try {
                Thread.sleep(7000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public static SSLSocketFactory socketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create a SSL socket factory", e);
        }
    }
}
