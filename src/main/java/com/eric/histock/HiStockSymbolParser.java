package com.eric.histock;

import com.eric.domain.CMQuote;
import com.eric.domain.Symbol;
import com.eric.histock.util.HiStockTechDataParser;
import com.eric.parser.Parser;
import com.eric.parser.ParserError;
import com.eric.parser.ParserResult;
import com.eric.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class HiStockSymbolParser implements Parser<Symbol> {

    //https://histock.tw/stock/module/stockdata.aspx?m=stocks&mid=0

    private static final String SYMBOL_URL = "https://histock.tw/stock/module/stockdata.aspx?m=stocks&mid=0";
    private Connection.Response response;

    public HiStockSymbolParser() {
    }


    @Override
    public ParserResult getResult() {
        ParserResult<Symbol> result = new ParserResult<>();
        String jsonStr;
        try {
            jsonStr = this.getResponse().body();
            ObjectMapper mapper = JsonUtils.getMapper();
            JsonNode jsonNode = mapper.readValue(jsonStr, JsonNode.class);
            Iterator<JsonNode> iterator = jsonNode.elements();
            while (iterator.hasNext()) {
                JsonNode element = iterator.next();
                Symbol symbol = Symbol.of(element);
                result.getResultList().add(symbol);
            }
            result.setSuccess(true);
            return result;
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(ParserError.JSOUP_PARSE_ERROR);
            log.error("", e);

        }
        return result;
    }

    @Override
    public String getUrl() {
        return SYMBOL_URL;
    }

    Connection.Response getResponse() throws IOException {
        response = Jsoup.connect(getUrl())
                .header("Referer", "https://histock.tw/stock/rank.aspx")
                .sslSocketFactory(socketFactory()).ignoreContentType(true).execute();
        return response;
    }

    private SSLSocketFactory socketFactory() {
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
