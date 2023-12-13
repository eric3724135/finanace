package com.eric.persist.pojo;

import com.eric.domain.Period;
import com.eric.domain.Quote;
import com.eric.domain.Symbol;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
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
@Entity
@Table(name = "quote")
public class QuoteDto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private long seq;
    /**
     * symbol
     */
    @Column(name = "symbol")
    private String symbol;
    /**
     * symbol_name
     */
    @Column(name = "name")
    private String name;
    /**
     * tradeDate
     */
    @Column(name = "trade_date")
    private LocalDate tradeDate;
    /**
     * open
     */
    @Column(name = "open")
    private double open;
    /**
     * high
     */
    @Column(name = "high")
    private double high;
    /**
     * low
     */
    @Column(name = "low")
    private double low;
    /**
     * close
     */
    @Column(name = "close")
    private double close;
    /**
     * period
     */
    @Column(name = "period")
    private String period;
    /**
     * diff
     */
    @Column(name = "diff")
    private double diff;
    /**
     * volume
     */
    @Column(name = "volume")
    private double volume;
    /**
     * avg_price
     */
    @Column(name = "avg_price")
    private double avgPrice = 0;
    /**
     * ma5
     */
    @Column(name = "ma5")
    private double ma5;
    /**
     * ma10
     */
    @Column(name = "ma10")
    private double ma10 = 0;
    /**
     * ma20
     */
    @Column(name = "ma20")
    private double ma20;
    /**
     * ma60
     */
    @Column(name = "ma60")
    private double ma60;
    /**
     * ma120
     */
    @Column(name = "ma120")
    private double ma120;
    /**
     * k9
     */
    @Column(name = "k9")
    private double k9;
    /**
     * d9
     */
    @Column(name = "d9")
    private double d9;
    /**
     * kdDiff
     */
    @Column(name = "kdDiff")
    private double kdDiff;
    /**
     * rsi5
     */
    @Column(name = "rsi5")
    private double rsi5;
    /**
     * rsi10
     */
    @Column(name = "rsi10")
    private double rsi10;


    @Transient
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMddHHmm");

    @Transient
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Symbol getSymbolObj() {
        Symbol result = new Symbol();
        result.setId(symbol);
        result.setName(name);
        return result;
    }


    public Quote getQuoteObj() {
        Quote quote = new Quote();
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
        return quote;
    }
}
