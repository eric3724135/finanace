package com.eric.executor;


import com.eric.common.StrategyResult;

import java.util.Map;

public interface StrategyExecutor<T> {

    public static final String PARAM_DATA = "data";

    StrategyResult<T> run(Map<String, String> params);

    String getStrategyName();

}
