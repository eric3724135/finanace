package com.eric.domain;

import lombok.Getter;

@Getter
public enum FVGPosition {

    HOLD("持有"),
    EMPTY("空手"),

    BUY("買入"),
    SELL("買入");

    private String name;

    FVGPosition(String name) {
        this.name = name;
    }

}
