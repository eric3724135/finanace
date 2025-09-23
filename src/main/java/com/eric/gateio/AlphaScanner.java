package com.eric.gateio;

import com.eric.utils.JsonUtils;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
public class AlphaScanner {

    private static String URL = "https://www.gate.com/zh-tw/announcements/alpha";

    private static final String BARK_URL = "https://api.day.app/DbWGKUmwoB95PnVpJdcafk/";

    private static List<String> CHECKED_LIST = new ArrayList<>();
    private static Date checkedDate = null;

    public static void main(String[] args) {


        AlphaScanner.scan();

    }


    public static void scan() {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        while (true) {
            log.info("gate 空投監控開始 {}", new Date());
            Calendar current = Calendar.getInstance();
            if (current.after(cal)) {
                try {
                    Jsoup.connect(BARK_URL + "gate 空投監控還活著！！")
                            .sslSocketFactory(socketFactory())
                            .ignoreContentType(true).execute();


                } catch (IOException e) {
                    e.printStackTrace();
                }
                cal.add(Calendar.HOUR_OF_DAY, 1);
                //Thread.sleep(10000);
            }
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -2);
            checkedDate = calendar.getTime();
            try {
                Document document = Jsoup.connect(URL).sslSocketFactory(socketFactory()).ignoreContentType(true)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36")
                        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                        .header("accept-language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-CN;q=0.6")
                        .referrer("https://www.gate.com/zh-tw/announcements/article/47096")
                        .cookie("_dx_uzZo5y", "1fb1f84a57bef219f06cecab16aff03b127c694d392aa7850d6983c3a7a6271119012b73")
                        .cookie("_ga", "GA1.2.581534502.1758590249")
                        .cookie("_ga_JNHPQJS9Q4", "GS2.2.s1758590249$o1$g0$t1758590249$j60$l0$h0")
                        .cookie("_gid", "GA1.2.1408581285.1758590249")
                        .cookie("finger_print", "68d1f52akqmtbQDiviUn9EwdtOxzopkvSrCkLLV1")
                        .cookie("lang", "tw")
                        .cookie("lasturl", "%2Fcryptoloan")
                        .cookie("RT", "\"z=1&dm=www.gate.com&si=6ef8f91d-e109-4bab-a036-17e09fe3cc78&ss=mfvv85kd&sl=1&tt=2vq&rl=1&ld=2vt\"")
                        .get();

//
                Element element = document.selectFirst("script#__NEXT_DATA__");
                if (element != null) {
                    String jsonStr = element.html();
                    ObjectMapper mapper = JsonUtils.getMapper();
                    mapper.configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS.mappedFeature(), true);
                    JsonNode objNode = mapper.readValue(jsonStr, JsonNode.class);
                    JsonNode dataNode = objNode.get("props").get("pageProps").get("listData").get("list");
                    ArrayNode dataArrNode = (ArrayNode) dataNode;
                    for (JsonNode data : dataArrNode) {
                        String title = data.get("title").asText();
                        if (title.contains("積分空投")) {
                            if (checkedDate == null) {
                                checkedDate = new Date(data.get("release_timestamp").asLong() * 1000);
                            }
                            Date releaseDate = new Date(data.get("release_timestamp").asLong() * 1000);
                            if (releaseDate.after(checkedDate)) {
                                Jsoup.connect(BARK_URL + data.get("title").asText())
                                        .sslSocketFactory(socketFactory())
                                        .ignoreContentType(true).execute();
                                checkedDate = releaseDate;
                            }

                        }

                    }
//                       log.info("jsonStr: {}", jsonStr);
                } else {
                    log.warn("找不到 script#__NEXT_DATA__");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(600000);
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
