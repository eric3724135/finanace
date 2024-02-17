package com.eric.service;

import com.eric.domain.Quote;
import com.eric.strategy.WWayStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StrategyService {

    @Autowired
    private WWayStrategy wWayStrategy;

    public boolean analysisWWayStrategy(List<Quote> quotes) {
        return wWayStrategy.analysis(quotes);
    }
}
