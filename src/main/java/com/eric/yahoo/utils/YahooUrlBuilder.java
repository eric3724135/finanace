package com.eric.yahoo.utils;


import java.util.Map;

public class YahooUrlBuilder {

    private static final String URL_TEMPLATE = "https://tw.quote.finance.yahoo.net/quote/q?type=ta&mkt=10&v=1";
    private static final String FUNC_TEMPLATE = "&%s=%s";
//    &perd=30 &sym=2330

    public static String build(Map<ApiFunc, String> funcMap) {
        StringBuilder builder = new StringBuilder();
        builder.append(URL_TEMPLATE);
        for (Map.Entry<ApiFunc, String> entry : funcMap.entrySet()) {
            builder.append(String.format(FUNC_TEMPLATE, entry.getKey().getCode(), entry.getValue()));
        }
        return builder.toString();
    }

    public enum ApiFunc {
        PERIOD("perd"),
        SYMBOL("sym"),
        ;

        private String code;

        ApiFunc(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
