package com.eric.apple;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class AppleCookieManager {

    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36";
    private static final String STORE_BASE_URL = "https://www.apple.com/tw/shop";
    private static final String PICKUP_PING_URL = "https://www.apple.com/tw/shop/retail/pickup-message?parts.0=MK2D4ZP/A&store=R713";
    private static final String DEFAULT_REFERER = "https://www.apple.com/tw/shop/buy-iphone";

    private static final String COOKIE_FILE_PATH = "apple-cookies.txt";

    private final Map<String, String> cookies = new ConcurrentHashMap<>();
    private final Map<String, String> defaultHeaders = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private long refreshIntervalMillis = 15 * 60 * 1000L; // 預設 15 分鐘更新一次 cookies

    private volatile long lastRefreshTime = 0;
    private long lastFileModifiedTime = 0;

    private ScheduledExecutorService scheduler;

    public AppleCookieManager() {
        this(15, TimeUnit.MINUTES);
    }

    public AppleCookieManager(long refreshInterval, TimeUnit unit) {
        this.refreshIntervalMillis = unit.toMillis(refreshInterval);
        initDefaultHeaders();
        initDefaultCookies();
        refreshCookies(); // 啟動時先取得一次最新 cookies
    }

    private void initDefaultHeaders() {
        defaultHeaders.put("User-Agent", DEFAULT_USER_AGENT);
        defaultHeaders.put("accept", "*/*");
        defaultHeaders.put("accept-encoding", "gzip, deflate, br, zstd");
        defaultHeaders.put("accept-language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-CN;q=0.6");
        defaultHeaders.put("referer", DEFAULT_REFERER);
        defaultHeaders.put("sec-ch-ua",
                "\"Chromium\";v=\"139\", \"Not;A=Brand\";v=\"24\", \"Google Chrome\";v=\"139\"");
        defaultHeaders.put("sec-ch-ua-mobile", "?0");
        defaultHeaders.put("sec-ch-ua-platform", "\"macOS\"");
        defaultHeaders.put("sec-fetch-dest", "empty");
        defaultHeaders.put("sec-fetch-mode", "cors");
        defaultHeaders.put("sec-fetch-site", "same-origin");
    }

    private void initDefaultCookies() {
        // 設定台灣商店必要的固定標記 cookie
        cookies.put("geo", "TW");
        cookies.put("as_sfa", "Mnx0d3x0d3x8emhfVFd8Y29uc3VtZXJ8aW50ZXJuZXR8MHwwfDE");
        cookies.put("dssf", "1");
        cookies.put("as_uct", "0");
        cookies.put("s_cc", "true");
    }

    /**
     * 啟動背景定時刷新 cookies 排程
     */
    public synchronized void startScheduledRefresh() {
        startScheduledRefresh(this.refreshIntervalMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 啟動背景定時刷新 cookies 排程（指定間隔）
     */
    public synchronized void startScheduledRefresh(long interval, TimeUnit unit) {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
        this.refreshIntervalMillis = unit.toMillis(interval);
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AppleCookie-Refresher");
            t.setDaemon(true);
            return t;
        });

        long initialDelay = Math.max(0, refreshIntervalMillis - (System.currentTimeMillis() - lastRefreshTime));
        scheduler.scheduleWithFixedDelay(this::refreshCookies, initialDelay, refreshIntervalMillis,
                TimeUnit.MILLISECONDS);
        log.info("已啟動 Apple Cookies 定期更新排程，更新週期: {} 秒", unit.toSeconds(interval));
    }

    /**
     * 停止背景排程
     */
    public synchronized void stopScheduledRefresh() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
            log.info("已停止 Apple Cookies 定期更新排程");
        }
    }

    /**
     * 重置為全新乾淨的 Session（丟棄被風控或過期的動態 cookies）
     */
    public synchronized void resetToCleanSession() {
        log.warn("重置 Apple Session Cookies 為全新乾淨狀態...");
        cookies.clear();
        initDefaultCookies();
        loadCookiesFromFileIfPresent();
        lastRefreshTime = 0;
    }

    /**
     * 檢查是否需要更新 cookies，若超過間隔則自動更新
     */
    public void checkAndRefresh() {
        if (System.currentTimeMillis() - lastRefreshTime > refreshIntervalMillis) {
            refreshCookies();
        }
    }

    /**
     * 強制刷新 cookies（例如遇到 529/541 或驗證失敗時調用）
     */
    public void forceRefresh() {
        log.warn("強制更新 Apple Cookies...");
        resetToCleanSession();
        refreshCookies();
    }

    /**
     * 執行更新 cookies 邏輯：
     * 1. 檢查並讀取外部 apple-cookies.txt（若存在）
     * 2. 發送請求至 Apple 商店頁面與 pickup API 獲取最新動態 Set-Cookie
     */
    public synchronized void refreshCookies() {
        long now = System.currentTimeMillis();
        // 防抖：距離上次更新不足 30 秒時不重複打擾 Apple 伺服器
        if (lastRefreshTime > 0 && now - lastRefreshTime < 30_000L) {
            return;
        }

        log.info("開始取得最新 Apple Cookies... (前次更新: {})",
                lastRefreshTime == 0 ? "尚未更新" : new Date(lastRefreshTime));

        // 1. 若外部有 apple-cookies.txt 設定檔，優先讀取
        loadCookiesFromFileIfPresent();

        // 2. 主動向 Apple 官方發送請求取得最新 Session Cookies (僅在狀態碼 200 時採納)
        try {
            AppleHttpClient httpClient = new AppleHttpClient();
            Map<String, String> reqHeaders = new HashMap<>(defaultHeaders);
            reqHeaders.put("Cache-Control", "no-cache");
            reqHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
            reqHeaders.put("Sec-Fetch-Dest", "document");
            reqHeaders.put("Sec-Fetch-Mode", "navigate");
            reqHeaders.put("Sec-Fetch-Site", "none");

            // 訪問 Apple 商店首頁
            String refreshUrl = STORE_BASE_URL + "?_ts=" + now;
            AppleHttpClient.HttpResponse response = httpClient.get(refreshUrl, reqHeaders, cookies);
            if ((response.getStatusCode() == 200 || response.getStatusCode() == 301 || response.getStatusCode() == 302)
                    && response.getCookies() != null && !response.getCookies().isEmpty()) {
                cookies.putAll(response.getCookies());
                log.info("從 Apple 官方商店取得 {} 個動態 Cookies: {}", response.getCookies().size(),
                        response.getCookies().keySet());
            }

            lastRefreshTime = System.currentTimeMillis();
            log.info("Apple Cookies 刷新完成，目前共有 {} 個有效 Cookies", cookies.size());

        } catch (Exception e) {
            log.error("自 Apple 官網更新 Cookies 失敗: {}", e.getMessage());
        }
    }

    /**
     * 若外部存在 apple-cookies.txt，載入自訂 cookies
     */
    private void loadCookiesFromFileIfPresent() {
        File file = new File(COOKIE_FILE_PATH);
        if (!file.exists()) {
            return;
        }

        try {
            long modified = file.lastModified();
            if (modified > lastFileModifiedTime) {
                String content = Files.readString(file.toPath());
                parseAndMergeCookieString(content);
                lastFileModifiedTime = modified;
                log.info("已成功自外部檔案 {} 載入最新 Cookies", file.getAbsolutePath());
            }
        } catch (IOException e) {
            log.warn("讀取外部 cookie 檔案失敗: {}", e.getMessage());
        }
    }

    /**
     * 解析 key=value; 格式並更新到 cookies map
     */
    public void parseAndMergeCookieString(String rawCookieStr) {
        if (rawCookieStr == null || rawCookieStr.trim().isEmpty()) {
            return;
        }
        String[] parts = rawCookieStr.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            int equalIdx = trimmed.indexOf('=');
            if (equalIdx > 0) {
                String name = trimmed.substring(0, equalIdx).trim();
                String value = trimmed.substring(equalIdx + 1).trim();
                // 忽略非 cookie 屬性
                if (name.equalsIgnoreCase("Path") || name.equalsIgnoreCase("Domain")
                        || name.equalsIgnoreCase("Expires") || name.equalsIgnoreCase("Max-Age")
                        || name.equalsIgnoreCase("SameSite") || name.equalsIgnoreCase("Secure")
                        || name.equalsIgnoreCase("HttpOnly")) {
                    continue;
                }
                cookies.put(name, value);
            }
        }
    }

    /**
     * 每次 API 請求完成後，若有新的 response cookies，進行動態更新
     */
    public void updateCookiesFromResponse(Connection.Response response) {
        if (response == null) {
            return;
        }
        Map<String, String> resCookies = response.cookies();
        if (resCookies != null && !resCookies.isEmpty()) {
            cookies.putAll(resCookies);
        }
        List<String> setCookieHeaders = response.multiHeaders().get("Set-Cookie");
        if (setCookieHeaders != null) {
            for (String raw : setCookieHeaders) {
                parseAndMergeCookieString(raw);
            }
        }
    }

    /**
     * 取得已組裝好的 Cookie Header 字串
     */
    public String getCookieHeaderString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * 取得當前完整的 Cookies Map
     */
    public Map<String, String> getCookies() {
        return Collections.unmodifiableMap(cookies);
    }

    /**
     * 取得完整的模擬瀏覽器 Headers (包含最新動態 Cookie)
     */
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>(defaultHeaders);
        headers.put("cookie", getCookieHeaderString());
        return headers;
    }

    /**
     * 設定或新增客製化 Header
     */
    public void setHeader(String key, String value) {
        defaultHeaders.put(key, value);
    }

    /**
     * 手動注入或修改 Cookie
     */
    public void setCookie(String name, String value) {
        cookies.put(name, value);
    }

    public static SSLSocketFactory socketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create SSL socket factory", e);
        }
    }
}
