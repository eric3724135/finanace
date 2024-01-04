package com.eric.domain;

import com.eric.persist.pojo.QuoteDto;
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
public class Quote {

    public static final String US_QUOTE= "u";

    public static final String TWE_QUOTE= "t";

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
    private LocalDate tradeDate;
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
     * rsi5
     */
    private double rsi5;
    /**
     * rsi10
     */
    private double rsi10;
    /**
     * source
     */
    private String source;


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

    public  QuoteDto convertToQuoteDto(){
        QuoteDto quote = new QuoteDto();
        quote.setSymbol(symbol);
        quote.setName(name);
        quote.setTradeDate(tradeDate);
        quote.setPeriod(period);
        quote.setOpen(open);
        quote.setHigh(high);
        quote.setLow(low);
        quote.setClose(close);
        quote.setDiff(diff);
        quote.setVolume(volume);
        quote.setAvgPrice(avgPrice);
        quote.setMa5(ma5);
        quote.setMa10(ma10);
        quote.setMa20(ma20);
        quote.setMa60(ma60);
        quote.setMa120(ma120);
        quote.setK9(k9);
        quote.setD9(d9);
        quote.setKdDiff(kdDiff);
        quote.setRsi5(rsi5);
        quote.setRsi10(rsi10);
        quote.setSource(source);
        return quote;
    }

    public String getTradeDateStr() {
        return tradeDate.format(dateTimeFormatter);
    }

    public String getSimpleTradeDateStr() {
        return tradeDate.format(dateFormatter);
    }

    public static Quote buildSimpleQuote(Symbol symbol, LocalDate tradeDate) {
        Quote quote = new Quote();
        quote.setSymbol(symbol.getId());
        quote.setName(symbol.getName());
        quote.setTradeDate(tradeDate);
        return quote;
    }

    public static Quote of(Symbol symbol, Period period, JsonNode node) {
        Quote quote = new Quote();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        quote.setSymbol(symbol.getId());
        quote.setName(symbol.getName());
        quote.setPeriod(period.getCode());
        LocalDate date = LocalDate.parse(node.get("Date").textValue(), formatter);
        quote.setTradeDate(date);
        quote.setOpen(node.get("OpenPr").doubleValue());
        quote.setHigh(node.get("HighPr").doubleValue());
        quote.setLow(node.get("LowPr").doubleValue());
        quote.setClose(node.get("ClosePr").doubleValue());
        quote.setDiff(node.get("PriceDifference").doubleValue());
        quote.setVolume(node.get("DealQty").doubleValue());
        quote.setMa5(node.get("MA5").doubleValue());
        quote.setMa20(node.get("MA20").doubleValue());
        quote.setMa60(node.get("MA60").doubleValue());
        quote.setK9(node.get("K9").doubleValue());
        quote.setD9(node.get("D9").doubleValue());
        quote.setKdDiff(node.get("DIF").doubleValue());
        quote.setRsi5(node.get("RSI5").doubleValue());
        quote.setRsi10(node.get("RSI10").doubleValue());
        return quote;
    }

    public static Quote ofFromYahoo(Symbol symbol, Period period, JsonNode node) {
        Quote quote = new Quote();
        DateTimeFormatter formatter;
        if (Period.ONE_DAY.equals(period) || Period.ONE_WEEK.equals(period)) {
            formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            quote.setTradeDate(LocalDate.parse(node.get("t").asText(), formatter));
        }
        quote.setSymbol(symbol.getId());
        quote.setPeriod(period.getCode());
        quote.setName(symbol.getName());
        quote.setOpen(node.get("o").doubleValue());
        quote.setHigh(node.get("h").doubleValue());
        quote.setLow(node.get("l").doubleValue());
        quote.setClose(node.get("c").doubleValue());
        quote.setVolume(node.get("v").doubleValue());
        return quote;
    }

    public static Quote ofFromWango(Symbol symbol, Period period, JsonNode node) {
        Quote quote = new Quote();
        LocalDateTime date =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(node.get("time").asLong()), ZoneId.systemDefault());
        quote.setTradeDate(date.toLocalDate());
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

    public static Quote getMockData() throws JsonProcessingException {
        String str = "{\"Date\":\"20190911\",\"OpenPr\":264.0,\"HighPr\":264.5,\"LowPr\":260.5,\"ClosePr\":263.0,\"PriceDifference\":1.5,\"MagnitudeOfPrice\":0.57,\"MA5\":263.2,\"MA20\":255.78,\"MA60\":252.2,\"DealQty\":36266,\"K9\":80.23,\"D9\":80.66,\"DIF\":2.915,\"MACD\":1.985,\"DIF_MACD\":0.93,\"RSI5\":63.13,\"RSI10\":61.95}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(str, JsonNode.class);
        Symbol symbol = new Symbol();
        symbol.setId("2330");
        symbol.setName("台積電");
        Quote quote = Quote.of(symbol, Period.ONE_DAY, node);
        log.info(quote.toString());
        return quote;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quote quote = (Quote) o;
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
        Quote quote = Quote.getMockData();

    }
}
