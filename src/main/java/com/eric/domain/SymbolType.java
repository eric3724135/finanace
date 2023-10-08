package com.eric.domain;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum SymbolType {
    STOCK_EXCHANGE(1, "上市"),
    OVER_THE_COUNTER(2, "上櫃"),
    EMERGING_STOCK(3, "興櫃");

    private int code;

    private String desc;

    SymbolType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static Map<Integer, SymbolType> map = new HashMap<>();

    static {
        for (SymbolType key : SymbolType.values())
            map.put(key.code, key);
    }

    public static SymbolType getByCode(int code) {
        return map.get(code);
    }

}
