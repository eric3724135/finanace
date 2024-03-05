package com.eric.wessiorfinance.util;

import com.eric.domain.Symbol;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Data
public class WessiorFintechTL {

    private Symbol symbol;

    private LocalDate date;

    /**
     * 偏差值
     */
    private double deviation;
    /**
     * 標準值
     */
    private double std;

    /**
     * 評估值
     */
    private double tl;

    /**
     * 收盤價
     */
    private double close;

    public double getPositiveSTD() {
        return tl + std;
    }

    public double getDoublePositiveSTD() {
        return tl + std + std;
    }

    public double getNegtiveSTD() {
        return tl - std;
    }

    public double getDoubleNegtiveSTD() {
        return tl - std - std;
    }


    public static WessiorFintechTL of(Symbol symbol, JsonNode node) {
        WessiorFintechTL tlObj = new WessiorFintechTL();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        tlObj.setSymbol(symbol);
        LocalDate date = LocalDate.parse(node.get("theDate_O").textValue(), formatter);
        tlObj.setDate(date);
        tlObj.setDeviation(node.get("DEVIATION").doubleValue());
        tlObj.setStd(node.get("STD").doubleValue());
        tlObj.setTl(node.get("TL").doubleValue());
        tlObj.setClose(node.get("theClose").doubleValue());
        return tlObj;
    }


}
