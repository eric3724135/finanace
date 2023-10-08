package com.eric.histock;

import com.eric.domain.Symbol;
import com.eric.parser.Parser;
import com.eric.parser.ParserResult;
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

public class HiStockAbstractParser<T> implements Parser {

    private static final String URL_TEMPLATE = "https://histock.tw/stock/chip/chartdata.aspx?no=%s&days=%s&m=dailyk,close,volume,mean5,mean10,mean20,mean60,mean120,mean5volume,mean20volume,k9,d9,rsi6,rsi12,dif,macd,osc";
    private Connection.Response response;
    private Symbol symbol;
    private int days;

    public HiStockAbstractParser(Symbol symbol, int days) {
        this.symbol = symbol;
        this.days = days;
    }

    @Override
    public String getUrl() {
        return String.format(URL_TEMPLATE, symbol.getId(), days);
    }

    @Override
    public ParserResult getResult() {
        return null;
    }

    Connection.Response getResponse() throws IOException {
        response = Jsoup.connect(getUrl()).sslSocketFactory(socketFactory()).ignoreContentType(true).execute();

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

    public Symbol getSymbol() {
        return symbol;
    }
}
