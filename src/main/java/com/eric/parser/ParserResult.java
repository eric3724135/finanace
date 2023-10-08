package com.eric.parser;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParserResult<T> {
    
    private boolean success = false;
    
    private ParserError error;
    
    private List<T> resultList = new ArrayList<>();
}
