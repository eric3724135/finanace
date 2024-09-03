package com.eric.service;

import com.eric.domain.*;
import com.eric.persist.pojo.FVGRecordDto;
import com.eric.persist.pojo.FavoriteSymbolDto;
import com.eric.persist.repo.FVGRecordRepository;
import com.eric.persist.repo.FavoriteSymbolRepository;
import com.eric.strategy.CupHandleStrategy;
import com.eric.strategy.FVGStrategy;
import com.eric.strategy.WWayStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class StrategyService {

    @Autowired
    private WWayStrategy wWayStrategy;
    @Autowired
    private CupHandleStrategy cupHandleStrategy;
    @Autowired
    private FavoriteSymbolRepository favoriteSymbolRepository;
    @Autowired
    private QuoteService quoteService;

    public boolean analysisWWayStrategy(List<Quote> quotes) {
        return wWayStrategy.analysis(quotes);
    }

    public List<Quote> analysisCupAndHandle(String symbol) {
        Optional<FavoriteSymbolDto> optionalSymbol = favoriteSymbolRepository.findById(symbol);
        if (optionalSymbol.isPresent()) {
            FavoriteSymbolDto symbolDto = optionalSymbol.get();
            List<Quote> quotes = null;
            if ("0".equals(symbolDto.getType())) {
                quotes = quoteService.getusQuotesFromSite(Symbol.ofTW(symbolDto.getId(), symbolDto.getName()), "1d", "10y");
                if (quotes == null || quotes.isEmpty()) {
                    quotes = quoteService.getusQuotesFromSite(Symbol.ofTWO(symbolDto.getId(), symbolDto.getName()), "1d", "10y");
                }
            } else {
                quotes = quoteService.getusQuotesFromSite(symbolDto.getSymbolObj(), "1d", "10y");
            }
            Collections.reverse(quotes);
            List<Quote> results = cupHandleStrategy.findCupAndHandlePatterns(quotes);
            return results;
        }

        return new ArrayList<>();
    }

}
