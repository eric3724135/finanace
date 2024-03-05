package com.eric.wessiorfinance.util;

import lombok.Getter;

@Getter
public enum TLStatus {

    UP_TO_2SD("高於兩倍樂觀"),
    BETWEEN_SD_TO_2SD("介於一至兩倍樂觀"),
    BETWEEN_TL_TO_SD("介於標準至樂觀"),
    BETWEEN_N_SD_TO_TL("介於悲觀至標準"),
    BETWEEN_N_2SD_TO_N_SD("介於一至兩倍悲觀"),
    LOW_TO_N_2SD("低於兩倍悲觀"),
    UNKNOWN("未知"),
    ;

    private final String desc;

    TLStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
