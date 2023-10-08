package com.eric.parser;

public interface Parser<T> {
    
    String getUrl();
    
    ParserResult<T> getResult();
    
}
