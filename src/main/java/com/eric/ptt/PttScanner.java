package com.eric.ptt;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
public class PttScanner {

    private static String URL = "https://www.ptt.cc/bbs/MacShop/index.html";

    private static final String BARK_URL = "";

    private static List<String> CHECKED_LIST = new ArrayList<>();

    public static void main(String[] args) {
        String[] keyWords = new String[]{"徵求", "16", "max", "256"};


        PttScanner.scan(keyWords);

    }



    public static void scan(String[] keyWords) {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        while (true) {
            log.info("PTT查詢開始 {}", new Date());
            Calendar current = Calendar.getInstance();
            if (current.after(cal)) {
                try {
                    Jsoup.connect(BARK_URL + "PTT 服務還活著！！")
                            .sslSocketFactory(socketFactory())
                            .ignoreContentType(true).execute();


                } catch (IOException e) {
                    e.printStackTrace();
                }
                cal.add(Calendar.HOUR_OF_DAY, 1);
                //Thread.sleep(10000);
            }

            try {
                Document document = Jsoup.connect(URL).sslSocketFactory(socketFactory()).ignoreContentType(true).get();

                Elements elements = document.select("div.title");
                for (int i = 1; i < elements.size(); i++) {
                    try {
                        Elements herfElements = elements.get(i).children();
                        String title = herfElements.get(0).text();
                        String herf = herfElements.attr("href");
                        boolean match = true;
                        for (String key : keyWords) {
                            if (!title.contains(key)) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            if (!CHECKED_LIST.contains(herf)) {
                                //新符合的條件
                                CHECKED_LIST.add(herf);
                                //通知
                                Jsoup.connect(BARK_URL + title)
                                        .sslSocketFactory(socketFactory())
                                        .ignoreContentType(true).execute();
                            }
                        }
                    }catch (Exception e){

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(60000);
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
