package com.eric.wessiorfinance.util;

import com.eric.domain.Symbol;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Data
public class WessiorFintechTunnel {

    private Symbol symbol;

    private LocalDate date;

    private double ma20;

    private double upBound;

    private double lowerBound;

    private double kValue;

    private double dValue;




    public static WessiorFintechTunnel of(Symbol symbol, JsonNode node) {
        WessiorFintechTunnel obj = new WessiorFintechTunnel();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        obj.setSymbol(symbol);
        LocalDate date = LocalDate.parse(node.get("theDate_O").textValue(), formatter);
        obj.setDate(date);
        obj.setMa20(node.get("MA20").doubleValue());
        obj.setUpBound(node.get("UB").doubleValue());
        obj.setLowerBound(node.get("LB").doubleValue());
        obj.setKValue(node.get("K").doubleValue());
        obj.setDValue(node.get("D").doubleValue());
        return obj;
    }


}
