package com.eric.yahoo;

import com.eric.domain.USQuote;
import com.eric.domain.UsSymbol;
import com.eric.parser.Parser;
import com.eric.parser.ParserError;
import com.eric.parser.ParserResult;
import com.eric.utils.JsonUtils;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

@Slf4j
public class YahooUSQuoteParser implements Parser {

    //台股 ex 2330.tw(上市) 8924.two(櫃買)
    private static final String URL_TEMPLATE = "https://partner-query.finance.yahoo.com/v8/finance/chart/%s?range=%s&interval=%s";
    //"1d",
    //"5d",
    //"1mo",
    //"3mo",
    //"6mo",
    //"1y",
    //"2y",
    //"5y",
    //"10y",
    //"ytd",
    //"max"
    private static final String RANGE = "1mo";
    private static final String INTERVAL = "1d";
    private final DecimalFormat df = new DecimalFormat("##.00");

    private Connection.Response response;
    @Getter
    private final UsSymbol symbol;
    private final String range;
    private final String interval;

    public YahooUSQuoteParser(UsSymbol symbol, String range, String interval) {
        this.symbol = symbol;
        this.range = range;
        this.interval = interval;
    }

    @Override
    public String getUrl() {
        if (StringUtils.isNotBlank(range)) {
            return String.format(URL_TEMPLATE, symbol.getSymbol(), range, interval);
        } else {
            return String.format(URL_TEMPLATE, symbol.getSymbol(), RANGE, interval);
        }
    }

    @Override
    public ParserResult<USQuote> getResult() {
        ParserResult<USQuote> result = new ParserResult<>();
        List<USQuote> quotes = result.getResultList();
        String jsonStr = "";
        try {
            //金鑰跟header "Refer"ˊ值有相關
            jsonStr = this.getResponse().body();
            ObjectMapper mapper = JsonUtils.getMapper();
            mapper.configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS.mappedFeature(), true);
            JsonNode dataNode = mapper.readValue(jsonStr, JsonNode.class);
            JsonNode arrNode = dataNode.get("chart").get("result");
            if (arrNode.isArray()) {
                for (JsonNode node : arrNode) {
                    JsonNode timeArrNode = node.get("timestamp");
                    if (timeArrNode.isArray()) {
                        for (JsonNode timeNode : timeArrNode) {
                            USQuote quote = new USQuote();
                            LocalDateTime localDateTime =
                                    LocalDateTime.ofInstant(Instant.ofEpochMilli(timeNode.asLong() * 1000), TimeZone
                                            .getDefault().toZoneId()).toLocalDate().atStartOfDay();
                            quote.setTradeDate(localDateTime.toLocalDate());
                            quote.setSymbol(symbol.getSymbol());
                            quote.setName(symbol.getName());
                            quotes.add(quote);
                        }
                    }
                    JsonNode quoteArrNode = node.get("indicators").get("quote");
                    if (quoteArrNode.isArray()) {
                        for (JsonNode quoteNode : quoteArrNode) {
                            JsonNode lowArrNode = quoteNode.get("low");
                            if (lowArrNode.isArray()) {
                                for (int i = 0; i < lowArrNode.size(); i++) {
                                    quotes.get(i).setLow(Double.parseDouble(df.format(lowArrNode.get(i).asDouble())));
                                }
                            }
                            JsonNode volumeArrNode = quoteNode.get("volume");
                            if (volumeArrNode.isArray()) {
                                for (int i = 0; i < volumeArrNode.size(); i++) {
                                    quotes.get(i).setVolume(Double.parseDouble(df.format(volumeArrNode.get(i).asDouble())));
                                }
                            }
                            JsonNode closeArrNode = quoteNode.get("close");
                            if (closeArrNode.isArray()) {
                                for (int i = 0; i < closeArrNode.size(); i++) {
                                    quotes.get(i).setClose(Double.parseDouble(df.format(closeArrNode.get(i).asDouble())));
                                }
                            }
                            JsonNode highArrNode = quoteNode.get("high");
                            if (highArrNode.isArray()) {
                                for (int i = 0; i < highArrNode.size(); i++) {
                                    quotes.get(i).setHigh(Double.parseDouble(df.format(highArrNode.get(i).asDouble())));
                                }
                            }
                            JsonNode openArrNode = quoteNode.get("open");
                            if (openArrNode.isArray()) {
                                for (int i = 0; i < openArrNode.size(); i++) {
                                    quotes.get(i).setOpen(Double.parseDouble(df.format(openArrNode.get(i).asDouble())));
                                }
                            }
                        }
                    }
                }
            }

            result.setSuccess(true);
            return result;
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(ParserError.JSOUP_PARSE_ERROR);
            log.error(jsonStr == null ? "" : jsonStr, e);
            log.error("[{}] {}", getSymbol().getSymbol(), getSymbol().getName(), e);

        }
        return result;
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

}
