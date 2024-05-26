package com.eric.service;

import com.eric.domain.*;
import com.eric.persist.pojo.FVGRecordDto;
import com.eric.persist.pojo.FavoriteSymbolDto;
import com.eric.persist.repo.FVGRecordRepository;
import com.eric.strategy.FVGStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FVGService {


    @Autowired
    private SymbolService symbolService;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private FVGStrategy strategy;

    @Autowired
    private FVGRecordRepository fvgRecordRepository;

    public List<FVGRecordDto> findRecordByRange(LocalDate startDate, LocalDate endDate) {
        return fvgRecordRepository.findRecordByRange(startDate, endDate);
    }

    @Scheduled(cron = "0 0 15 * * ?")
    public void scheduleFVGStrategy() {
        log.info("FVG 策略啟動");

        List<FavoriteSymbolDto> tweList = symbolService.getFavoriteSymbols(SymbolType.TWE);
        List<FavoriteSymbolDto> usList = symbolService.getFavoriteSymbols(SymbolType.US);

        for (FavoriteSymbolDto favoriteSymbolDto : tweList) {
            Symbol symbol = Symbol.ofTW(favoriteSymbolDto.getId(), favoriteSymbolDto.getName());
            List<Quote> quotes = new ArrayList<>();
            quotes = quoteService.getusQuotesFromSite(symbol, "1d", "6mo");
            if (quotes == null || quotes.isEmpty()) {
                symbol = Symbol.ofTWO(favoriteSymbolDto.getId(), favoriteSymbolDto.getName());
                quotes = quoteService.getusQuotesFromSite(symbol, "1d", "6mo");
            }
            if (quotes == null || quotes.size() < 10) {
                continue;
            }
            List<FVGResult> results = strategy.execute(symbol.getId(), quotes);

//            FVGResult result = results.get(results.size() - 1);
            for (int i = results.size() - 10; i < results.size() - 1; i++) {
                FVGResult result = results.get(i);

                if (FVGPosition.BUY.equals(result.getPosition()) || FVGPosition.SELL.equals(result.getPosition())) {
                    //寫入紀錄
                    List<FVGRecordDto> records = fvgRecordRepository.findByIdAndTradeDate(favoriteSymbolDto.getId(), result.getTradeDate());
                    if (records.isEmpty()) {
                        FVGRecordDto record = new FVGRecordDto();
                        record.setId(favoriteSymbolDto.getId());
                        record.setName(favoriteSymbolDto.getName());
                        record.setTradeDate(result.getTradeDate());
                        record.setClose(result.getClose());
                        record.setUpAvg(result.getUpAvgValue());
                        record.setDownAvg(record.getDownAvg());
                        record.setPosition(result.getPosition().name());
                        fvgRecordRepository.save(record);
                    }
                }
            }
        }
        for (FavoriteSymbolDto favoriteSymbolDto : usList) {
            Symbol symbol = favoriteSymbolDto.getSymbolObj();
            List<Quote> quotes = quoteService.getusQuotesFromSite(symbol, "1d", "6mo");
            if (quotes == null || quotes.size() < 10) {
                continue;
            }
            List<FVGResult> results = strategy.execute(symbol.getId(), quotes);

//            FVGResult result = results.get(results.size() - 1);
            for (int i = results.size() - 10; i < results.size() - 1; i++) {
                FVGResult result = results.get(i);

                if (FVGPosition.BUY.equals(result.getPosition()) || FVGPosition.SELL.equals(result.getPosition())) {
                    //寫入紀錄
                    List<FVGRecordDto> records = fvgRecordRepository.findByIdAndTradeDate(favoriteSymbolDto.getId(), result.getTradeDate());
                    if (records.isEmpty()) {
                        FVGRecordDto record = new FVGRecordDto();
                        record.setId(favoriteSymbolDto.getId());
                        record.setName(favoriteSymbolDto.getName());
                        record.setTradeDate(result.getTradeDate());
                        record.setClose(result.getClose());
                        record.setUpAvg(result.getUpAvgValue());
                        record.setDownAvg(record.getDownAvg());
                        record.setPosition(result.getPosition().name());
                        fvgRecordRepository.save(record);
                    }
                }
            }
        }

    }

}
