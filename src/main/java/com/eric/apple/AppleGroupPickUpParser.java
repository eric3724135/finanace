package com.eric.apple;

import com.eric.parser.Parser;
import com.eric.parser.ParserResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class AppleGroupPickUpParser implements Parser<String> {

    //    private static final String URL_TEMPLATE = "https://www.apple.com/tw/shop/fulfillment-messages?store=R713&little=false&mt=regular&parts.0=%s";
    private static final String URL_TEMPLATE = "https://www.apple.com/tw/shop/fulfillment-messages?pl=true&mts.0=compact&%ssearchNearby=true&store=R713";


    private String hookUrl = "";

    private String barkUrl = "";
    private String discordUrl = "";

    private String url;
    private final Map<String, String> params;
    private final boolean isNotice = false;


    public AppleGroupPickUpParser(Map<String, String> params, String hookUrl, String barkUrl, String discordUrl) {
        this.params = params;
        this.barkUrl = barkUrl;
        this.hookUrl = hookUrl;
        this.discordUrl = discordUrl;
        this.handleUrl(new ArrayList<>(params.keySet()));
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public ParserResult<String> getResult() {
        ParserResult<String> result = new ParserResult<>();
        if (isNotice) {
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
                    JsonNode prodsNode = storeNode.get("partsAvailability");
                    for (String prodKey : params.keySet()) {
                        boolean canPick = prodsNode.get(prodKey).get("messageTypes").get("compact").get("storeSelectionEnabled").asBoolean();
                        if (canPick) {
//                            String url = String.format(hookUrl, params.get(prodKey) + " 有貨囉!!");
//                            Jsoup.connect(url)
//                                    .sslSocketFactory(socketFactory())
//                                    .ignoreContentType(true).execute();
//                            Jsoup.connect(discordUrl)
//                                    .data("content", params.get(prodKey) + " 有貨囉!!")
//                                    .post();
                            Jsoup.connect(barkUrl + params.get(prodKey) + " 有貨囉!!")
                                    .sslSocketFactory(socketFactory())
                                    .ignoreContentType(true).execute();
                        }
                    }
                }
            }
        } catch (HttpStatusException he) {
            log.warn("APPLE 服務529 ");
        } catch (Exception e) {
            log.error("[ERROR] {}", url, e);

        }

        return result;
    }


    private void handleUrl(List<String> prodKeys) {

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < prodKeys.size(); i++) {
            builder.append(String.format("parts.%s=%s&", i, prodKeys.get(i)));
        }


        this.url = String.format(URL_TEMPLATE, builder);

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
