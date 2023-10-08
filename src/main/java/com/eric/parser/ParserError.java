package com.eric.parser;


import lombok.Getter;

@Getter
public enum ParserError {

    JSOUP_PARSE_ERROR("C00001","網頁爬蟲失敗"),
    JSOUP_SYMBOL_ERROR("C00002","股票有誤");
    
    private String code;
    
    private String desc;
    
    ParserError(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
