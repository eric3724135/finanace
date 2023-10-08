package com.eric.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
@Data
public class CMQuote {

    private long seq;
    /**
     * symbol
     */
    private String symbol;
    /**
     * symbol_name
     */
    private String name;
    /**
     * tradeDate
     */
    private LocalDateTime tradeDate;
    /**
     * period
     */
    private String period;
    /**
     * open
     */
    private double open;
    /**
     * high
     */
    private double high;
    /**
     * low
     */
    private double low;
    /**
     * close
     */
    private double close;
    /**
     * diff
     */
    private double diff;
    /**
     * volume
     */
    private double volume;
    /**
     * magnitude_price
     */
    private double magnitudePrice;
    /**
     * avg_price
     */
    private double avgPrice = 0;
    /**
     * ma5
     */
    private double ma5;
    /**
     * ma10
     */
    private double ma10 = 0;
    /**
     * ma20
     */
    private double ma20;
    /**
     * ma60
     */
    private double ma60;
    /**
     * ma120
     */
    private double ma120;
    /**
     * k9
     */
    private double k9;
    /**
     * d9
     */
    private double d9;
    /**
     * kdDiff
     */
    private double kdDiff;
    /**
     * macd
     */
    private double macd;
    /**
     * osc(macd 柱圖資料)
     */
    private double osc;
    /**
     * macdDiff
     */
    private double macdDiff;
    /**
     * rsi5
     */
    private double rsi5;
    /**
     * rsi10
     */
    private double rsi10;
    /**
     * mtm10
     */
    private Double mtm10;
    /**
     * mtm_ma10
     */
    private Double mtmMa10;
    /**
     * mtm10p
     */
    private Double mtm10p;
    /**
     * mtmMa10p
     */
    private Double mtmMa10p;


//    @Transient
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMddHHmm");

//    @Transient
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Symbol getSymbolObj(){
        Symbol result = new Symbol();
        result.setId(symbol);
        result.setName(name);
        return result;
    }

    public String getTradeDateStr() {
        return tradeDate.format(dateTimeFormatter);
    }

    public String getSimpleTradeDateStr() {
        return tradeDate.format(dateFormatter);
    }

    public static CMQuote buildSimpleQuote(Symbol symbol, LocalDateTime tradeDate) {
        CMQuote quote = new CMQuote();
        quote.setSymbol(symbol.getId());
        quote.setName(symbol.getName());
        quote.setTradeDate(tradeDate);
        return quote;
    }

    public static CMQuote of(Symbol symbol, Period period, JsonNode node) {
        CMQuote quote = new CMQuote();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        quote.setSymbol(symbol.getId());
        quote.setName(symbol.getName());
        quote.setPeriod(period.getCode());
        LocalDate date = LocalDate.parse(node.get("Date").textValue(), formatter);
        quote.setTradeDate(date.atStartOfDay());
        quote.setOpen(node.get("OpenPr").doubleValue());
        quote.setHigh(node.get("HighPr").doubleValue());
        quote.setLow(node.get("LowPr").doubleValue());
        quote.setClose(node.get("ClosePr").doubleValue());
        quote.setDiff(node.get("PriceDifference").doubleValue());
        quote.setVolume(node.get("DealQty").doubleValue());
        quote.setMagnitudePrice(node.get("MagnitudeOfPrice").doubleValue());
        quote.setMa5(node.get("MA5").doubleValue());
        quote.setMa20(node.get("MA20").doubleValue());
        quote.setMa60(node.get("MA60").doubleValue());
        quote.setK9(node.get("K9").doubleValue());
        quote.setD9(node.get("D9").doubleValue());
        quote.setKdDiff(node.get("DIF").doubleValue());
        quote.setMacd(node.get("MACD").doubleValue());
        quote.setMacdDiff(node.get("DIF_MACD").doubleValue());
        quote.setOsc(quote.getMacdDiff() - quote.getMacd());
        quote.setRsi5(node.get("RSI5").doubleValue());
        quote.setRsi10(node.get("RSI10").doubleValue());
        return quote;
    }

    public static CMQuote ofFromYahoo(Symbol symbol, Period period, JsonNode node) {
        CMQuote quote = new CMQuote();
        DateTimeFormatter formatter;
        if (Period.ONE_DAY.equals(period) || Period.ONE_WEEK.equals(period)) {
            formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            quote.setTradeDate(LocalDate.parse(node.get("t").asText(), formatter).atStartOfDay());
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            quote.setTradeDate(LocalDateTime.parse(node.get("t").asText(), formatter));
        }
        quote.setSymbol(symbol.getId());
        quote.setPeriod(period.getCode());
        quote.setName(symbol.getName());
        quote.setOpen(node.get("o").doubleValue());
        quote.setHigh(node.get("h").doubleValue());
        quote.setLow(node.get("l").doubleValue());
        quote.setClose(node.get("c").doubleValue());
        quote.setVolume(node.get("v").doubleValue());
        if (Period.ONE_HOUR.equals(period) &&
                quote.getTradeDate().getMinute() > 0 &&
                quote.getTradeDate().getHour() != 13) {
            return null;
        }
        return quote;
    }

    public static CMQuote ofFromWango(Symbol symbol, Period period, JsonNode node) {
        CMQuote quote = new CMQuote();
        LocalDateTime date =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(node.get("time").asLong()), ZoneId.systemDefault());
        quote.setTradeDate(date);
        quote.setSymbol(symbol.getId());
        quote.setPeriod(period.getCode());
        quote.setName(symbol.getName());
        quote.setOpen(node.get("open").doubleValue());
        quote.setHigh(node.get("high").doubleValue());
        quote.setLow(node.get("low").doubleValue());
        quote.setClose(node.get("close").doubleValue());
        quote.setVolume(node.get("volume").doubleValue());
        return quote;
    }

    public static CMQuote getMockData() throws JsonProcessingException {
        String str = "{\"Date\":\"20190911\",\"OpenPr\":264.0,\"HighPr\":264.5,\"LowPr\":260.5,\"ClosePr\":263.0,\"PriceDifference\":1.5,\"MagnitudeOfPrice\":0.57,\"MA5\":263.2,\"MA20\":255.78,\"MA60\":252.2,\"DealQty\":36266,\"K9\":80.23,\"D9\":80.66,\"DIF\":2.915,\"MACD\":1.985,\"DIF_MACD\":0.93,\"RSI5\":63.13,\"RSI10\":61.95}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(str, JsonNode.class);
        Symbol symbol = new Symbol();
        symbol.setId("2330");
        symbol.setName("台積電");
        CMQuote quote = CMQuote.of(symbol, Period.ONE_DAY, node);
        log.info(quote.toString());
        return quote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CMQuote quote = (CMQuote) o;
        return symbol.equals(quote.symbol) &&
                name.equals(quote.name) &&
                tradeDate.equals(quote.tradeDate) &&
                period.equals(quote.period);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, name, tradeDate, period);
    }

    public static void main(String[] args) throws JsonProcessingException {
        CMQuote quote = CMQuote.getMockData();

    }
}
