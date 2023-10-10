package com.eric.yahoo;

import com.eric.domain.Period;
import com.eric.domain.Symbol;
import com.eric.parser.Parser;
import com.eric.parser.ParserResult;
import com.eric.yahoo.utils.YahooUrlBuilder;
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
import java.util.HashMap;
import java.util.Map;

public class YahooAbstractParser<T> implements Parser {


    private Connection.Response response;
    private Symbol symbol;
    private Period period;
    private Map<YahooUrlBuilder.ApiFunc, String> urlParamMap = new HashMap<>();

    public YahooAbstractParser(Symbol symbol, Period period) {
        this.symbol = symbol;
        this.period = period;
        if (symbol != null)
            urlParamMap.put(YahooUrlBuilder.ApiFunc.SYMBOL, symbol.getId());
        if (period != null)
            urlParamMap.put(YahooUrlBuilder.ApiFunc.PERIOD, period.getCode());
    }

    @Override
    public String getUrl() {
        return YahooUrlBuilder.build(urlParamMap);
    }

    @Override
    public ParserResult<T> getResult() {
        return null;
    }

    Connection.Response getResponse() throws IOException {
        return Jsoup.connect(getUrl()).sslSocketFactory(socketFactory()).ignoreContentType(true).execute();
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

    public Symbol getSymbol() {
        return symbol;
    }

    public Period getPeriod() {
        return period;
    }

    public Map<YahooUrlBuilder.ApiFunc, String> getUrlParamMap() {
        return urlParamMap;
    }
}
