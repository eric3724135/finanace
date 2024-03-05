package com.eric.wessiorfinance;

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

public class WFAbstractParser<T> implements Parser {

    private Connection.Response response;

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public ParserResult getResult() {
        return null;
    }

    Connection.Response getResponse() throws IOException {
        response = Jsoup.connect(getUrl())
                .header("referer", "https://invest.wessiorfinance.com/notation.html")
                .sslSocketFactory(socketFactory())
                .ignoreContentType(true)
                .execute();

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
