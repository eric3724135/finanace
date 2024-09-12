package com.eric.apple;

import com.eric.parser.Parser;
import com.eric.parser.ParserResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Slf4j
public class ApplePickUpParser implements Parser<String> {
    //TODO 改用AppleGroupPickUpParser service
//    private static final String URL_TEMPLATE = "https://www.apple.com/tw/shop/fulfillment-messages?store=R713&little=false&mt=regular&parts.0=%s";
    private static final String URL_TEMPLATE = "https://www.apple.com/tw/shop/fulfillment-messages?pl=true&mts.0=compact&parts.0=%s&searchNearby=true&store=R713";


    public static final String postUrl = "";

    private String url;
    private String prodKey;
    @Getter
    private String prodName;
    private boolean isNotice = false;


    public ApplePickUpParser(String prodKey, String prodName) {
        this.prodKey = prodKey;
        this.prodName = prodName;
        this.handleUrl(prodKey);
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public ParserResult<String> getResult() {
        ParserResult<String> result = new ParserResult<>();
        if(isNotice){
            return result;
        }
        try {
            Connection.Response response = Jsoup.connect(getUrl()).sslSocketFactory(socketFactory()).ignoreContentType(true).execute();
            String body = response.body();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(body, JsonNode.class);
            if ("200".equals(node.get("head").get("status").asText())) {
                JsonNode storeArrNode = node.get("body").get("content").get("pickupMessage").get("stores");
                ArrayNode arrayNode = (ArrayNode) storeArrNode;
                for (JsonNode storeNode : arrayNode) {
                    JsonNode prodNode = storeNode.get("partsAvailability").get(prodKey);
                    boolean canPick = prodNode.get("messageTypes").get("compact").get("storeSelectionEnabled").asBoolean();
                    if (canPick) {
//                        String url = String.format(postUrl, prodName + " 有貨囉!!");
//                        Jsoup.connect(url)
//                                .sslSocketFactory(socketFactory())
//                                .ignoreContentType(true).execute();
                        // IOS APP【BARK】
                        Jsoup.connect("" + prodName + " 有貨囉!!")
                                .sslSocketFactory(socketFactory())
                                .ignoreContentType(true).execute();
                    }
                }
            }
        } catch (HttpStatusException he) {
            log.warn("APPLE {} 服務529 ", prodName);
            //try {
            //  Thread.sleep(5000);
            //} catch (InterruptedException e) {
            //  e.printStackTrace();
            //}
        } catch (Exception e) {
            log.error("[ERROR] {}", url, e);

        }

        return result;
    }


    private void handleUrl(String source) {
        this.url = String.format(URL_TEMPLATE, source);
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
