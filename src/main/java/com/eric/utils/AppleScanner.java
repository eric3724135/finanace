package com.eric.utils;

import com.eric.apple.AppleCookieManager;
import com.eric.apple.AppleGroupPickUpParser;
import com.eric.apple.AppleHttpClient;
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
import java.util.concurrent.TimeUnit;

@Slf4j
public class AppleScanner {

    private static final String MAKE_URL = "";
    private static final String DISCORD_HOOK = "";
    private static final String BARK_URL = "";

    // Cookies 定期更新週期 (預設 15 分鐘)
    private static final long COOKIE_REFRESH_INTERVAL_MINUTES = 15;

    // IP 協議 (IPv6 <-> IPv4) 主動定時輪換週期 (預設 10 分鐘，主動輪換避開單一 IP 連續請求 15~17 分鐘觸發 541)
    private static final long IP_ROTATION_INTERVAL_MINUTES = 10;

    public static void main(String[] args) {
        Map<String, String> prodMap = new HashMap<>();

        prodMap.put("MYWX3ZP/A", "16 max 256 沙");
        //prodMap.put("MYWW3ZP/A", "16 max 256 白");
        //prodMap.put("MYWY3ZP/A", "16 max 256 原");
        //prodMap.put("MYWV3ZP/A", "16 max 256 黑");
        try {
            AppleScanner.appleGroupProductParser(prodMap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void appleGroupProductParser(Map<String, String> params) throws InterruptedException {
        List<String> prodKeys = new ArrayList<>(params.keySet());

        // 0. 初始化主動定時 IP 輪換 (每 10 分鐘主動切換 IPv6 <-> IPv4，讓另一 IP 進入 10 分鐘靜默冷卻期)
        com.eric.apple.AppleHttpClient.setEnableProactiveRotation(true);
        com.eric.apple.AppleHttpClient.setIpRotationIntervalMinutes(IP_ROTATION_INTERVAL_MINUTES);
        com.eric.apple.AppleHttpClient.resetIpRotationTimer();
        log.info("已啟用主動 IP 定時輪換機制：每 {} 分鐘自動於 IPv6 與 IPv4 間主動切換，預防進入 541 風控！", IP_ROTATION_INTERVAL_MINUTES);

        // 1. 初始化 CookieManager，固定時間（例如 15 分鐘）自動自 Apple 官網更新 Cookies
        AppleCookieManager cookieManager = new AppleCookieManager(COOKIE_REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES);
        cookieManager.startScheduledRefresh();

        // 註冊關閉 Hook，優雅結束排程執行緒
        Runtime.getRuntime().addShutdownHook(new Thread(cookieManager::stopScheduledRefresh));

        // 2. 自訂模擬瀏覽器 Header
        Map<String, String> browserHeaders = new HashMap<>();
        browserHeaders.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
        browserHeaders.put("Accept", "application/json, text/javascript, */*; q=0.01");
        browserHeaders.put("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-CN;q=0.6");
        browserHeaders.put("Referer", "https://www.apple.com/tw/shop/buy-iphone");
        browserHeaders.put("X-Requested-With", "XMLHttpRequest");

        // 3. 建立帶有動態 Cookie 管理與 Header 的 AppleGroupPickUpParser
        AppleGroupPickUpParser pickUpParser = new AppleGroupPickUpParser(
                params, MAKE_URL, BARK_URL, DISCORD_HOOK, cookieManager, browserHeaders);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        while (true) {
            log.info("查詢開始 {} [目前協議: {}]", new Date(), AppleHttpClient.getCurrentIpProtocol());
            Calendar current = Calendar.getInstance();
            if (current.after(cal)) {
                try {
                    Jsoup.connect(BARK_URL + " APPLE 服務還活著！！")
                            .sslSocketFactory(socketFactory())
                            .ignoreContentType(true).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cal.add(Calendar.HOUR_OF_DAY, 1);
            }

            // 執行查詢 (Parser 內部自動使用最新 Cookies 與 Headers，並在 529 時自癒刷新)
            pickUpParser.getResult();

            try {
                // 安全查詢間隔 28 秒 + 隨機 1~3 秒抖動，維持在 Apple WAF 安全頻率窗口內
                long jitter = 1000 + (long) (Math.random() * 2000);
                Thread.sleep(28000 + jitter);
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
