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
                        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36")
                        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                        .header("accept-language", "zh-TW,zh;q=0.9")
                        .header("accept-encoding", "gzip, deflate, br, zstd")
                        .header("cache-control", "max-age=0")
                        .header("if-none-match", "\"qwafm379zl6kci\"")
                        .header("cookie","dssid2=70165175-0511-454b-87d0-ed97f888a386; dssf=1; as_pcts=yC2c15pFPUl0l++9B1brtBe4GD:82xAKjrOE4oF-TENWi08IQp+PWQVObt0f+tDHLS0zovRjtCRycBEgMTq_A1LYggnO5+ACOWawJ+aUsu7OnanXQ0e26CMR; as_affl=p240%7C%7Cken_pid%3A%3Ago~cmp-245082782~adg-12618487622~ad-824083957840_aud-2468689073717%3Akwd-10778630~dev-c~ext-~prd-~mca-~nt-search%26cid%3A%3Aaos-tw-kwgo-txt-brand---%26token%3A%3A8c1a76ab-cfe5-4380-b551-e755a3b48bc8%26%7C%7C20261021_062400; geo=TW; as_sfa=Mnx0d3x0d3x8emhfVFd8Y29uc3VtZXJ8aW50ZXJuZXR8MHwwfDE; s_afc=p240%7Cgo%7Ecmp-245082782%7Eadg-12618487622%7Ead-824083957840_aud-2468689073717%3Akwd-10778630%7Edev-c%7Eext-%7Eprd-%7Emca-%7Ent-search; s_campaign=aos-tw-kwgo-txt-brand---; s_cc=true; bnc-uuid=f5fe7e47-d607-4a95-996f-01aa5da779ab; ac_bnd=10; as_uct=0; as_dc=ucp3; shld_bt_m=DInCk83c1qq7r0rLfDbw6w|1792942832|nBigPRMdo1OsbWms_4HZrA|T_ssCezDiCkfaskRyYawvlnxT8U; s_fid=4D3660DD51DAD5A3-24DED5D8C918253B; as_atb=1.0|MjAyNi0xMC0yNSAwNjo0MDozMw|fd3890882215a6469532e91d57c0a4fb33f137f8; as_rumid=cd57b757-147f-45b5-964c-6d5668f6d94b; s_vi=[CS]v1|356F03692F888BAE-60000ED80EA38463[CE]; sh_spksy=.; shld_bt_ck=GDKYLsFCh0vKBCYudbCx3A|1792942835|R_ICxqMulQFJVKSSX3Rr7ttgdVLW9FMGp63FO5881_mdVgo_THP1Bmnkblr-5t6CusBcoQFo5k1h37XwTOD4xYVq9xlwymQxYid6pirn2gtUxLWiedYJoXVGLFi_CdUuBwTFlN-Ty-jcRRkR50JatyJIHl4pt_7tZGvz2h7_kCAIoD_4S_mokBLnT_0S6IQvNaUg7nX8eE-GQ3jiNiAfSx-EtFsqOT3l9pIza2XyQVczcF1ihpptR9QyVzplu-HJuFLHvmCOly8mXdscCPhydN9iLjNb8vocpcD3zB3PbwKnp8IG6qETWQIHFlGuIuPEtTP-d-4F4AnKGQa_YUD4SAGN1gwvW-iueRoodSmI7BiKQ0NM1gPnn0Jnf4MkEw8xxpN50um6eHpg1IWzjJv2PG-W2gazd_Xn3IDPckxqP3IsCi5ej81wc4tRjVcsD_XCM7a2uEtRkb89ibCKMkJWygd5kp7UeAqU2RA-yLhGZzOcsheG0ecO4o84FVQrXHICdl1T3j7SKg8Tb0yImuEUkuCLHudCTwwQIERnxZj1sq31Bx0vrAX2q8u6AqNjmT6BmSZuyemUOPvOxR091RgWOPOf7RmT0lE5QCjZSGYJUkM|UHNnBwpZCF5KXCI_Dsk7TC0p2Vc; s_sq=%5B%5BB%5D%5D")
                        .referrer("https://www.apple.com/tw/shop/buy-iphone/iphone-duo/7.6-%E5%90%8B%E9%A1%AF%E7%A4%BA%E5%99%A8-512gb-%E5%A4%9C%E7%A9%BA%E8%89%B2\n")
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
