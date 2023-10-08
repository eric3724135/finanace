package com.eric.domain;

import lombok.Data;

@Data
public class RsiResult {

    private String symbol;

    private String stockType;

    private String futureType;

    private String pastDateStr;

    private double pastClose;

    private double pastRsi;

    private double pastMa120;

    private String currentDateStr;

    private double currentClose;

    private double currentRsi;

    private double currentMa120;

    private double futureParam;

    private double futureResult;

    public void setPast(CMQuote quote){
        currentClose = quote.getClose();
        currentRsi = quote.getRsi5();
        currentDateStr = quote.getSimpleTradeDateStr();
        currentMa120 = quote.getMa120();
    }

    public void setCurrent(CMQuote quote){
        pastClose = quote.getClose();
        pastRsi = quote.getRsi5();
        pastDateStr = quote.getSimpleTradeDateStr();
        pastMa120 = quote.getMa120();
    }
}
