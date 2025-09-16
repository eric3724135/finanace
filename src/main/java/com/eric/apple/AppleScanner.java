package com.eric.apple;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

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
public class AppleScanner {

    private static final String MAKE_URL = "";
    private static final String DISCORD_HOOK = "";
    private static final String BARK_URL = "";

    public static void main(String[] args) {
        Map<String, String> prodMap = new HashMap<>();
        prodMap.put("MG8H4ZP/A","17 pro 256 橘");
        prodMap.put("MG8G4ZP/A", "17 pro 256 銀");
        prodMap.put("MG8J4ZP/A", "17 pro 256 藍");
        prodMap.put("MFYN4ZP/A","17 max 256 橘");
        prodMap.put("MFYM4ZP/A", "17 max 256 銀");
        prodMap.put("MFYP4ZP/A", "17 max 256 藍");

        try {

            AppleScanner.appleGroupProductParser(prodMap);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void appleGroupProductParser(Map<String, String> params) throws InterruptedException {
        List<String> prodKeys = new ArrayList<>();
        for (Map.Entry<String, String> prod : params.entrySet()) {
            prodKeys.add(prod.getKey());
        }
        AppleGroupPickUpParser pickUpParser = new AppleGroupPickUpParser(params, MAKE_URL, BARK_URL, DISCORD_HOOK);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        try {
            Jsoup.connect(BARK_URL + "偵測啟動!!")
                    .sslSocketFactory(socketFactory())
                    .ignoreContentType(true).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            log.info("查詢開始 {}", new Date());
            Calendar current = Calendar.getInstance();
            if (current.after(cal)) {
                try {
                    Jsoup.connect(BARK_URL + "office APPLE 服務還活著！！")
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
