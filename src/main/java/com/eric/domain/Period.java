package com.eric.domain;

import java.util.HashMap;
import java.util.Map;

public enum Period {
    ONE_MIN("1m"),
    FIVE_MIN("5m"),
    FIFTEEN_MIN("15m"),
    THIRTY_MIN("30m"),
    ONE_HOUR("60m"),
    ONE_DAY("d"),
    ONE_WEEK("w"),
    ONE_MONTH("m"),
    ONE_SEASON("s"),
    ONE_YEAR("y");


    Period(String code) {
        this.code = code;
    }

    private String code;

    public String getCode() {
        return code;
    }

    private static Map<String, Period> map = new HashMap<>();

    static {
        for (Period key : Period.values())
            map.put(key.code, key);
    }

    public static Period getByCode(String code) {
        return map.get(code);
    }
}
