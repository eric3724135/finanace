package com.eric.domain;


import lombok.Data;

@Data
public class UsSymbol {
    private long seq;
    /**
     *
     */
    private String symbol;
    /**
     *
     */
    private String name;


    public UsSymbol(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public UsSymbol() {
    }
}
