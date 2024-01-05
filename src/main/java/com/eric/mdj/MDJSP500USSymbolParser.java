package com.eric.mdj;

import com.eric.domain.Symbol;
import com.eric.parser.Parser;
import com.eric.parser.ParserError;
import com.eric.parser.ParserResult;
import com.eric.utils.JsonUtils;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Slf4j
public class MDJSP500USSymbolParser implements Parser<Symbol> {

    //https://www.moneydj.com/us/rest/list0003a2/GSPC

    private static final String SYMBOL_URL = "https://www.moneydj.com/us/rest/list0003a2/GSPC";
    private Connection.Response response;

    public MDJSP500USSymbolParser() {
    }


    @Override
    public ParserResult<Symbol> getResult() {
        ParserResult<Symbol> result = new ParserResult<>();
        String jsonStr = "";
        try {
            jsonStr = Jsoup.connect(getUrl())
                    .sslSocketFactory(socketFactory()).ignoreContentType(true).execute().body();
            ObjectMapper mapper = JsonUtils.getMapper();
            mapper.configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS.mappedFeature(), true);
            JsonNode arrNode = mapper.readValue(jsonStr, JsonNode.class);
            if (arrNode.isArray()) {
                for (JsonNode node : arrNode) {
                    String symbol = node.get("id").textValue();
                    String name = node.get("name").textValue();
                    Symbol obj = Symbol.ofUS(symbol, name);
                    result.getResultList().add(obj);
                }
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
