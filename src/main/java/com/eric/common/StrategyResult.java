package com.eric.common;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class StrategyResult<T> {

    private boolean success = false;

    private StrategyError error;

    private List<T> resultList = new ArrayList<>();

    /**
     * 新增排除條件列表
     */
    private Map<T,String> excludeMap = new HashMap<>();

}