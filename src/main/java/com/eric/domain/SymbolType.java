package com.eric.domain;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum SymbolType {
    TWE("0", "台股"),
    US("1", "美股");

    private String code;

    private String desc;

    SymbolType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static Map<String, SymbolType> map = new HashMap<>();

    static {
        for (SymbolType key : SymbolType.values())
            map.put(key.code, key);
    }

    public static SymbolType getByCode(String code) {
        return map.get(code);
    }

}
