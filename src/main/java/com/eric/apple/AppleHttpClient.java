package com.eric.apple;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 專為 Apple API 設計的 HTTP 請求客戶端。
 * 因 Apple 部署的 Akamai WAF 會對純 Java JVM 的 TLS Client Hello（JA3 指紋）進行封鎖並回傳 HTTP 541，
 * 本客戶端在 macOS/Linux 上優先採用原生系統級 TLS 連線（curl 引擎），完美模擬真實瀏覽器連線，
 * 徹底解決「瀏覽器開啟正常但 Java 程式回傳 541」的問題。
 */
@Slf4j
public class AppleHttpClient {

    private static final boolean IS_CURL_AVAILABLE;

    static {
        boolean available = false;
        try {
            Process p = new ProcessBuilder("curl", "--version").start();
            boolean finished = p.waitFor(2, TimeUnit.SECONDS);
            if (finished && p.exitValue() == 0) {
                available = true;
            }
        } catch (Exception e) {
            log.warn("本機未檢測到 curl，將降級使用 Jsoup: {}", e.getMessage());
        }
        IS_CURL_AVAILABLE = available;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HttpResponse {
        private int statusCode;
        private String body = "";
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> cookies = new HashMap<>();
    }

    /**
     * 發送 GET 請求
     */
    public HttpResponse get(String url, Map<String, String> headers, Map<String, String> cookies) throws Exception {
        if (IS_CURL_AVAILABLE) {
            return executeCurlGet(url, headers, cookies);
        } else {
            return executeJsoupGet(url, headers, cookies);
        }
    }

    private static final java.util.concurrent.atomic.AtomicBoolean USE_IPV6 = new java.util.concurrent.atomic.AtomicBoolean(true);
    private static final java.util.concurrent.atomic.AtomicBoolean ENABLE_IPV6_SWITCH = new java.util.concurrent.atomic.AtomicBoolean(true);
    private static final java.util.concurrent.atomic.AtomicBoolean ENABLE_PROACTIVE_ROTATION = new java.util.concurrent.atomic.AtomicBoolean(true);
    private static final java.util.concurrent.atomic.AtomicLong ROTATION_INTERVAL_MILLIS = new java.util.concurrent.atomic.AtomicLong(10 * 60 * 1000L); // 預設 10 分鐘
    private static final java.util.concurrent.atomic.AtomicLong lastIpSwitchTime = new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis());

    /**
     * 設定是否啟用 IPv6 優先或切換
     */
    public static void setEnableIpv6(boolean enable) {
        USE_IPV6.set(enable);
        ENABLE_IPV6_SWITCH.set(enable);
        log.info("AppleHttpClient IPv6 設定已更新: USE_IPV6={}, ENABLE_IPV6_SWITCH={}", enable, enable);
    }

    /**
     * 設定主動定時輪換間隔 (分鐘)
     */
    public static void setIpRotationIntervalMinutes(long minutes) {
        ROTATION_INTERVAL_MILLIS.set(minutes * 60 * 1000L);
        log.info("AppleHttpClient 主動 IP 輪換間隔設定為: {} 分鐘", minutes);
    }

    /**
     * 設定主動定時輪換間隔 (毫秒，供測試使用)
     */
    public static void setIpRotationIntervalMillis(long millis) {
        ROTATION_INTERVAL_MILLIS.set(millis);
        log.info("AppleHttpClient 主動 IP 輪換間隔設定為: {} 毫秒", millis);
    }

    /**
     * 設定是否啟用主動定時輪換 IP (預設 true)
     */
    public static void setEnableProactiveRotation(boolean enable) {
        ENABLE_PROACTIVE_ROTATION.set(enable);
        log.info("AppleHttpClient 主動定時 IP 輪換開關設定為: {}", enable);
    }

    /**
     * 重設定時輪換起算時間
     */
    public static void resetIpRotationTimer() {
        lastIpSwitchTime.set(System.currentTimeMillis());
    }

    /**
     * 取得當前使用的 IP 協議 ("IPv6" 或 "IPv4")
     */
    public static String getCurrentIpProtocol() {
        return USE_IPV6.get() ? "IPv6" : "IPv4";
    }

    private HttpResponse executeCurlGet(String url, Map<String, String> headers, Map<String, String> cookies) throws Exception {
        long now = System.currentTimeMillis();
        // 檢查是否達到主動定時輪換條件 (預設 10 分鐘自動對調 IPv6 <-> IPv4)
        if (ENABLE_PROACTIVE_ROTATION.get()) {
            long elapsed = now - lastIpSwitchTime.get();
            if (elapsed >= ROTATION_INTERVAL_MILLIS.get()) {
                boolean nextIpv6 = !USE_IPV6.get();
                USE_IPV6.set(nextIpv6);
                lastIpSwitchTime.set(now);
                long elapsedSeconds = elapsed / 1000;
                log.info("🔄【主動 IP 輪換】當前 IP 協議已持續使用 {} 秒，主動切換至 {} 進行查詢，讓另一 IP 進入靜默冷卻期！",
                        elapsedSeconds, nextIpv6 ? "IPv6" : "IPv4");
            }
        }

        boolean preferIpv6 = USE_IPV6.get();
        // 優先嘗試當前 IP 協議
        HttpResponse response = doCurlRequest(url, headers, cookies, preferIpv6);

        // 若回傳 541 且啟用雙棧切換，自動切換至另一 IP 協議 (IPv6 <-> IPv4)
        if (response.getStatusCode() == 541 && ENABLE_IPV6_SWITCH.get()) {
            boolean fallbackIpv6 = !preferIpv6;
            log.warn("檢測到 HTTP 541 (使用 {})，自動切換至 {} 進行即時容災重試: {}",
                    preferIpv6 ? "IPv6" : "IPv4", fallbackIpv6 ? "IPv6" : "IPv4", url);
            Thread.sleep(1500);
            response = doCurlRequest(url, headers, cookies, fallbackIpv6);
            if (response.getStatusCode() == 200) {
                USE_IPV6.set(fallbackIpv6);
                lastIpSwitchTime.set(System.currentTimeMillis()); // 容災切換後重設定時起算點
                log.info("切換至 {} 容災重試成功！後續請求已自動切換至 {}",
                        fallbackIpv6 ? "IPv6" : "IPv4", fallbackIpv6 ? "IPv6" : "IPv4");
            }
        }
        return response;
    }

    private HttpResponse doCurlRequest(String url, Map<String, String> headers, Map<String, String> cookies, boolean useIpv6) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add(useIpv6 ? "-6" : "-4");  // 雙棧切換支援：動態使用 IPv6 或 IPv4
        command.add("--http2");
        command.add("-s");  // silent
        command.add("-i");  // include protocol response headers
        command.add("--compressed");
        command.add("--connect-timeout");
        command.add("5");   // 快速連線逾時（若當前節點延遲高，快速切換下一節點）
        command.add("--max-time");
        command.add("30");  // 充足的整體請求時間避免誤殺連線

        // 使用不區分大小寫的 Map，防止 Referer/referer、Accept/accept 等 Header 重複傳送
        Map<String, String> mergedHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        mergedHeaders.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
        mergedHeaders.put("Accept", "application/json, text/javascript, */*; q=0.01");
        mergedHeaders.put("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7");
        mergedHeaders.put("Referer", "https://www.apple.com/tw/shop/buy-iphone");
        mergedHeaders.put("X-Requested-With", "XMLHttpRequest");
        mergedHeaders.put("Sec-Fetch-Dest", "empty");
        mergedHeaders.put("Sec-Fetch-Mode", "cors");
        mergedHeaders.put("Sec-Fetch-Site", "same-origin");

        if (headers != null) {
            mergedHeaders.putAll(headers);
        }

        // 組合 Cookies
        StringBuilder cookieHeader = new StringBuilder();
        if (cookies != null && !cookies.isEmpty()) {
            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                if (cookieHeader.length() > 0) {
                    cookieHeader.append("; ");
                }
                cookieHeader.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        if (mergedHeaders.containsKey("cookie")) {
            if (cookieHeader.length() > 0) {
                cookieHeader.append("; ");
            }
            cookieHeader.append(mergedHeaders.remove("cookie"));
        }

        for (Map.Entry<String, String> entry : mergedHeaders.entrySet()) {
            command.add("-H");
            command.add(entry.getKey() + ": " + entry.getValue());
        }

        if (cookieHeader.length() > 0) {
            command.add("-H");
            command.add("Cookie: " + cookieHeader.toString());
        }

        command.add(url);

        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();

        StringBuilder rawOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                rawOutput.append(line).append("\n");
            }
        }

        boolean finished = process.waitFor(35, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Curl request timed out for URL: " + url);
        }

        return parseCurlResponse(rawOutput.toString());
    }

    private HttpResponse parseCurlResponse(String raw) {
        HttpResponse response = new HttpResponse();
        if (raw == null || raw.isEmpty()) {
            return response;
        }

        // 分割 Headers 與 Body
        String[] sections = raw.split("\r?\n\r?\n");
        if (sections.length == 0) {
            return response;
        }

        // 最後一個區塊是 Body，之前的區塊可能是多次重定向的 Headers
        String headerSection = sections[0];
        StringBuilder bodyBuilder = new StringBuilder();
        if (sections.length > 1) {
            // 如果有多個 HTTP 回應頭（如 100 Continue 或 302），取最後一個有效狀態
            for (int i = 0; i < sections.length - 1; i++) {
                if (sections[i].startsWith("HTTP/")) {
                    headerSection = sections[i];
                }
            }
            for (int i = 1; i < sections.length; i++) {
                if (sections[i - 1].equals(headerSection)) {
                    for (int j = i; j < sections.length; j++) {
                        if (bodyBuilder.length() > 0) bodyBuilder.append("\n\n");
                        bodyBuilder.append(sections[j]);
                    }
                    break;
                }
            }
        }

        response.setBody(bodyBuilder.toString().trim());

        // 解析狀態碼與 Header
        String[] lines = headerSection.split("\r?\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (i == 0 && line.startsWith("HTTP/")) {
                String[] parts = line.split(" ");
                if (parts.length >= 2) {
                    try {
                        response.setStatusCode(Integer.parseInt(parts[1]));
                    } catch (NumberFormatException ignored) {}
                }
            } else {
                int colonIdx = line.indexOf(':');
                if (colonIdx > 0) {
                    String name = line.substring(0, colonIdx).trim();
                    String val = line.substring(colonIdx + 1).trim();
                    response.getHeaders().put(name, val);

                    if ("Set-Cookie".equalsIgnoreCase(name)) {
                        parseCookieLine(val, response.getCookies());
                    }
                }
            }
        }

        return response;
    }

    private void parseCookieLine(String rawCookie, Map<String, String> cookieMap) {
        String[] parts = rawCookie.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            int eq = trimmed.indexOf('=');
            if (eq > 0) {
                String name = trimmed.substring(0, eq).trim();
                String val = trimmed.substring(eq + 1).trim();
                if (!isCookieAttribute(name)) {
                    cookieMap.put(name, val);
                }
            }
        }
    }

    private boolean isCookieAttribute(String name) {
        return name.equalsIgnoreCase("Path") || name.equalsIgnoreCase("Domain")
                || name.equalsIgnoreCase("Expires") || name.equalsIgnoreCase("Max-Age")
                || name.equalsIgnoreCase("SameSite") || name.equalsIgnoreCase("Secure")
                || name.equalsIgnoreCase("HttpOnly");
    }

    private HttpResponse executeJsoupGet(String url, Map<String, String> headers, Map<String, String> cookies) throws Exception {
        Connection conn = Jsoup.connect(url)
                .sslSocketFactory(AppleCookieManager.socketFactory())
                .ignoreContentType(true)
                .timeout(10000);

        if (headers != null) {
            for (Map.Entry<String, String> h : headers.entrySet()) {
                conn.header(h.getKey(), h.getValue());
            }
        }
        if (cookies != null) {
            for (Map.Entry<String, String> c : cookies.entrySet()) {
                conn.cookie(c.getKey(), c.getValue());
            }
        }

        Connection.Response res = conn.execute();
        HttpResponse response = new HttpResponse();
        response.setStatusCode(res.statusCode());
        response.setBody(res.body());
        response.setCookies(new HashMap<>(res.cookies()));
        return response;
    }
}
