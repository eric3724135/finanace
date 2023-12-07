package com.eric.mdj;

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

public class MDJStockAbstractParser<T> implements Parser {

    private static final String URL_TEMPLATE = "https://money.moneydj.com/us/rank/rank0010";
    private Connection.Response response;

    public MDJStockAbstractParser() {
    }

    @Override
    public String getUrl() {
        return URL_TEMPLATE;
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

}
