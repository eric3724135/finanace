package com.eric.service;

import com.eric.domain.*;
import com.eric.persist.pojo.FVGRecordDto;
import com.eric.persist.pojo.FavoriteSymbolDto;
import com.eric.persist.repo.FVGRecordRepository;
import com.eric.strategy.FVGStrategy;
import com.eric.strategy.WWayStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class StrategyService {

    @Autowired
    private WWayStrategy wWayStrategy;

    public boolean analysisWWayStrategy(List<Quote> quotes) {
        return wWayStrategy.analysis(quotes);
    }

}
