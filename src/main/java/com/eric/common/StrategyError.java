package com.eric.common;

public enum StrategyError {
    STRATEGY_ILLEGAL_PARAM_ERROR("S00001", "參數錯誤"),
    STRATEGY_THIRD_PARTY_API_ERROR("S00002", "第三方API出錯"),
    STRATEGY_SERVICE_UNDEFINED_ERROR("S00003", "不支援此服務"),
    STRATEGY_UNKNOWN_ERROR("S99999", "策略未知錯誤");

    private String code;

    private String desc;

    StrategyError(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
