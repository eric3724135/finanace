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
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class AppleGroupPickUpParser implements Parser<String> {

    // 官方門市即時庫存查詢端點 (穩定不被 541 阻擋，自動查詢台灣全門市)
    private static final String URL_TEMPLATE_PICKUP =
            "https://www.apple.com/tw/shop/retail/pickup-message?%ssearchNearby=true&store=R713";

    private String hookUrl = "";
    private String barkUrl = "";
    private String discordUrl = "";

    private String primaryUrl;
    private final Map<String, String> params;
    private final boolean isNotice = false;

    private AppleCookieManager cookieManager;
    private Map<String, String> headers = new HashMap<>();

    // 失敗通知防抖（避免 541/529 連續狂發 Bark 通知）
    private long lastFailNoticeTime = 0;
    private static final long FAIL_NOTICE_INTERVAL = 5 * 60 * 1000L; // 5 分鐘內最多通知一次錯誤

    public AppleGroupPickUpParser(Map<String, String> params, String hookUrl, String barkUrl, String discordUrl) {
        this(params, hookUrl, barkUrl, discordUrl, new AppleCookieManager(), null);
    }

    public AppleGroupPickUpParser(Map<String, String> params, String hookUrl, String barkUrl, String discordUrl,
                                  AppleCookieManager cookieManager) {
        this(params, hookUrl, barkUrl, discordUrl, cookieManager, null);
    }

    public AppleGroupPickUpParser(Map<String, String> params, String hookUrl, String barkUrl, String discordUrl,
                                  AppleCookieManager cookieManager, Map<String, String> headers) {
        this.params = params;
        this.barkUrl = barkUrl;
        this.hookUrl = hookUrl;
        this.discordUrl = discordUrl;
        this.cookieManager = cookieManager != null ? cookieManager : new AppleCookieManager();
        if (headers != null) {
            this.headers.putAll(headers);
        }
        this.handleUrl(new ArrayList<>(params.keySet()));
    }

    @Override
    public String getUrl() {
        return primaryUrl;
    }

    @Override
    public ParserResult<String> getResult() {
        ParserResult<String> result = new ParserResult<>();
        if (isNotice) {
            return result;
        }

        // 確保 cookies 未過期，固定時間或逾期自動更新
        if (cookieManager != null) {
            cookieManager.checkAndRefresh();
        }

        // 執行查詢
        executeRequest(primaryUrl, result);

        return result;
    }

    private AppleHttpClient httpClient = new AppleHttpClient();
    private int consecutiveFailures = 0;

    private boolean executeRequest(String requestUrl, ParserResult<String> result) {
        try {
            Map<String, String> reqHeaders = new HashMap<>();
            if (this.headers != null) {
                reqHeaders.putAll(this.headers);
            }

            Map<String, String> reqCookies = new HashMap<>();
            if (cookieManager != null) {
                reqCookies.putAll(cookieManager.getCookies());
            }

            // 加入動態時間戳防止命中相同快取特徵
            String actualUrl = requestUrl + (requestUrl.contains("?") ? "&" : "?") + "_ts=" + System.currentTimeMillis();
            AppleHttpClient.HttpResponse response = httpClient.get(actualUrl, reqHeaders, reqCookies);
            int statusCode = response.getStatusCode();

            if (statusCode == 200) {
                consecutiveFailures = 0; // 重置失敗計數器

                // 僅在請求成功 (200) 時更新動態 Set-Cookie，防止 541/風控回應的異常 Cookie 污染 Session
                if (cookieManager != null && response.getCookies() != null && !response.getCookies().isEmpty()) {
                    for (Map.Entry<String, String> c : response.getCookies().entrySet()) {
                        cookieManager.setCookie(c.getKey(), c.getValue());
                    }
                }

                String body = response.getBody();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readValue(body, JsonNode.class);

                if (node.has("head") && "200".equals(node.get("head").get("status").asText())) {
                    result.setSuccess(true);
                    JsonNode storesArrNode = extractStoresNode(node);
                    if (storesArrNode != null && storesArrNode.isArray()) {
                        ArrayNode arrayNode = (ArrayNode) storesArrNode;
                        for (JsonNode storeNode : arrayNode) {
                            String storeName = storeNode.has("storeName") ? storeNode.get("storeName").asText() : "";
                            JsonNode prodsNode = storeNode.get("partsAvailability");
                            if (prodsNode == null) continue;

                            for (String prodKey : params.keySet()) {
                                JsonNode prodAvailability = prodsNode.get(prodKey);
                                if (prodAvailability == null) continue;

                                boolean canPick = checkCanPick(prodAvailability);
                                if (canPick) {
                                    String prodDesc = params.get(prodKey);
                                    String noticeText = (storeName.isEmpty() ? "" : "[" + storeName + "] ") + prodDesc + " 有貨囉!!";
                                    log.info("🎉 發現現貨: {}", noticeText);

                                    Jsoup.connect(barkUrl + noticeText)
                                            .sslSocketFactory(socketFactory())
                                            .ignoreContentType(true).execute();
                                }
                            }
                        }
                    } else {
                        log.debug("目前查詢的商品暫無門市供貨資料或門市列表為空");
                    }
                    return true;
                }
            } else {
                log.warn("APPLE API HTTP 狀態碼異常: {} (URL: {})", statusCode, requestUrl);
                handleErrorStatusCode(statusCode, requestUrl);
            }
        } catch (HttpStatusException he) {
            int statusCode = he.getStatusCode();
            log.warn("APPLE API HTTP 狀態碼異常: {} (URL: {})", statusCode, requestUrl);
            handleErrorStatusCode(statusCode, requestUrl);
        } catch (Exception e) {
            log.error("[ERROR] 請求或解析 Apple API 異常: {}", requestUrl, e);
        }
        return false;
    }

    private void handleErrorStatusCode(int statusCode, String requestUrl) {
        if (statusCode == 541 || statusCode == 529 || statusCode == 403 || statusCode == 401) {
            consecutiveFailures++;
            long cooldownSeconds = Math.min(180, 60 * consecutiveFailures);
            log.warn("狀態碼 {} 觸發 Session 重置，進入第 {} 次深度冷卻退避: {} 秒 (等待 Apple 頻率計數器歸零)...",
                    statusCode, consecutiveFailures, cooldownSeconds);

            if (cookieManager != null) {
                cookieManager.forceRefresh();
            }

            try {
                Thread.sleep(cooldownSeconds * 1000L);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        long now = System.currentTimeMillis();
        // 連續失敗 3 次以上且超過防抖間隔才通知，避免偶發風控頻繁警報
        if (consecutiveFailures >= 3 && now - lastFailNoticeTime > FAIL_NOTICE_INTERVAL) {
            lastFailNoticeTime = now;
            try {
                Jsoup.connect(barkUrl + "APPLE http status: " + statusCode + " (連續失敗" + consecutiveFailures + "次，自癒重試中)")
                        .sslSocketFactory(socketFactory())
                        .ignoreContentType(true).execute();
            } catch (IOException e) {
                log.error("[ERROR] 發送 Bark 通知失敗: {}", requestUrl, e);
            }
        }
    }

    /**
     * 相容解析 pickup-message (body.stores) 與 fulfillment-messages (body.content.pickupMessage.stores)
     */
    private JsonNode extractStoresNode(JsonNode root) {
        if (!root.has("body")) {
            return null;
        }
        JsonNode body = root.get("body");
        if (body.has("stores")) {
            return body.get("stores");
        }
        if (body.has("content") && body.get("content").has("pickupMessage")
                && body.get("content").get("pickupMessage").has("stores")) {
            return body.get("content").get("pickupMessage").get("stores");
        }
        return null;
    }

    /**
     * 檢查特定商品的門市取貨狀態 (相容 compact 與 regular 兩種格式)
     */
    private boolean checkCanPick(JsonNode prodAvailability) {
        if (!prodAvailability.has("messageTypes")) {
            return false;
        }
        JsonNode messageTypes = prodAvailability.get("messageTypes");
        if (messageTypes.has("compact") && messageTypes.get("compact").has("storeSelectionEnabled")) {
            if (messageTypes.get("compact").get("storeSelectionEnabled").asBoolean()) {
                return true;
            }
        }
        if (messageTypes.has("regular") && messageTypes.get("regular").has("storeSelectionEnabled")) {
            if (messageTypes.get("regular").get("storeSelectionEnabled").asBoolean()) {
                return true;
            }
        }
        return false;
    }

    private void handleUrl(List<String> prodKeys) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < prodKeys.size(); i++) {
            builder.append(String.format("parts.%s=%s&", i, prodKeys.get(i)));
        }
        this.primaryUrl = String.format(URL_TEMPLATE_PICKUP, builder);
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
