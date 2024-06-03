package com.eric.service;

import com.eric.domain.*;
import com.eric.excel.FVGStrategyExcelHandler;
import com.eric.mail.MailConfig;
import com.eric.mail.MailUtils;
import com.eric.persist.pojo.FVGRecordDto;
import com.eric.persist.pojo.FavoriteSymbolDto;
import com.eric.persist.repo.FVGRecordRepository;
import com.eric.strategy.FVGStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private MailConfig mailConfig;

    @Autowired
    private FVGRecordRepository fvgRecordRepository;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");


    public List<FVGRecordDto> findRecordByRange(LocalDate startDate, LocalDate endDate) {
        return fvgRecordRepository.findRecordByRange(startDate, endDate);
    }

    public List<FVGRecordDto> findRecordByPositionAndRange(String position, LocalDate startDate, LocalDate endDate) {
        return fvgRecordRepository.findRecordByPositionAndRange(position, startDate, endDate);
    }

    public FVGObject analysis(FVGRecordDto fvgRecord) {
        FVGObject object = FVGObject.of(fvgRecord);
        List<Quote> oriQuotes = quoteService.getusQuotesFromSite(new Symbol(object.getId(), object.getName()), "1d", "6mo");
        List<Quote> quotes = new ArrayList<>();
        for (Quote quote : oriQuotes) {
            if (quote.getTradeDate().isAfter(object.getTradeDate())) {
                quotes.add(quote);
            }
            if (quote.getTradeDate().isEqual(object.getTradeDate())) {
                object.setHighestQuote(quote);
                object.setLowestQuote(quote);
                object.setLatestQuote(quote);
            }
        }
        for (Quote quote : quotes) {
            if (quote.getHigh() > object.getHighestQuote().getHigh()) {
                object.setHighestQuote(quote);
            }
            if (quote.getLow() > object.getLowestQuote().getLow()) {
                object.setLowestQuote(quote);
            }
            if (quote.getTradeDate().isAfter(object.getLatestQuote().getTradeDate())) {
                object.setLatestQuote(quote);
            }
        }

        object.setHighestProfit(object.getHighestQuote().getHigh() / object.getClose() - 1);
        object.setLowestProfit(1 - object.getLowestQuote().getLow() / object.getClose());
        if (object.getLowestProfit() < 0) {
            object.setLowestProfit(0);
        }
        object.setLatestProfit(object.getLatestQuote().getClose() / object.getClose() - 1);


        return object;
    }

    @Scheduled(cron = "0 0 14 * * ?")
    public void scheduleFVGStrategy() {
        this.fetchFVGStrategy();
        List<FVGRecordDto> list = this.findRecordByRange(LocalDate.now(), LocalDate.now());
//        List<FVGObject> analysisList = new ArrayList<>();
//        for (FVGRecordDto recordDto : list) {
//            FVGObject object = this.analysis(recordDto);
//            analysisList.add(object);
//        }
        FVGStrategyExcelHandler handler = new FVGStrategyExcelHandler();
        try {
            ByteArrayOutputStream bos = handler.export(list);
            MailUtils.generateAndSendEmail(mailConfig, mailConfig.getAddressArr(),
                    String.format("%s_FVG每日挑檔", LocalDate.now().format(dateFormatter)),
                    String.format("%s_FVG每日挑檔", LocalDate.now().format(dateFormatter)),
                    String.format("%s_FVG.xlsx", LocalDate.now().format(dateFormatter)), bos);
        } catch (IOException | MessagingException e) {
            log.error("[FVGStrategyExcelHandler] error ", e);
        }


    }


    public void fetchFVGStrategy() {
        log.info("FVG 策略啟動");

        List<FavoriteSymbolDto> tweList = symbolService.getFavoriteSymbols(SymbolType.TWE);
        List<FavoriteSymbolDto> usList = symbolService.getFavoriteSymbols(SymbolType.US);


        for (FavoriteSymbolDto favoriteSymbolDto : tweList) {
            Symbol symbol = Symbol.ofTW(favoriteSymbolDto.getId(), favoriteSymbolDto.getName());
            List<Quote> quotes = quoteService.getusQuotesFromSite(symbol, "1d", "10y");
            if (quotes == null || quotes.isEmpty()) {
                symbol = Symbol.ofTWO(favoriteSymbolDto.getId(), favoriteSymbolDto.getName());
                quotes = quoteService.getusQuotesFromSite(symbol, "1d", "10y");
            }
            if (quotes == null || quotes.size() < 10) {
                continue;
            }
            List<FVGResult> results = strategy.execute(symbol.getId(), quotes);

//            FVGResult result = results.get(results.size() - 1);
            for (int i = results.size() - 10; i < results.size(); i++) {
                FVGResult result = results.get(i);

                if (FVGPosition.BUY.equals(result.getPosition()) || FVGPosition.SELL.equals(result.getPosition())) {

                    List<FVGRecordDto> records = fvgRecordRepository.findByIdAndTradeDate(symbol.getId(), result.getTradeDate());
                    if (!records.isEmpty()) {
                        if (result.getTradeDate().isEqual(LocalDate.now())) {
                            fvgRecordRepository.delete(records.get(0));
                            this.saveFvgRecord(symbol, favoriteSymbolDto, result);
                        }
                    }
                    //寫入紀錄
                    if (records.isEmpty()) {
                        this.saveFvgRecord(symbol, favoriteSymbolDto, result);
                    }
                }
            }
        }
        for (FavoriteSymbolDto favoriteSymbolDto : usList) {
            Symbol symbol = favoriteSymbolDto.getSymbolObj();
            List<Quote> quotes = quoteService.getusQuotesFromSite(symbol, "1d", "10y");
            if (quotes == null || quotes.size() < 10) {
                continue;
            }
            List<FVGResult> results = strategy.execute(symbol.getId(), quotes);

//            FVGResult result = results.get(results.size() - 1);
            for (int i = results.size() - 10; i < results.size(); i++) {
                FVGResult result = results.get(i);

                if (FVGPosition.BUY.equals(result.getPosition()) || FVGPosition.SELL.equals(result.getPosition())) {

                    List<FVGRecordDto> records = fvgRecordRepository.findByIdAndTradeDate(symbol.getId(), result.getTradeDate());
                    if (!records.isEmpty()) {
                        if (result.getTradeDate().isEqual(LocalDate.now())) {
                            fvgRecordRepository.delete(records.get(0));
                            this.saveFvgRecord(symbol, favoriteSymbolDto, result);
                        }
                    }
                    //寫入紀錄
                    if (records.isEmpty()) {
                        this.saveFvgRecord(symbol, favoriteSymbolDto, result);
                    }
                }
            }
        }

    }

    private void saveFvgRecord(Symbol symbol, FavoriteSymbolDto favoriteSymbolDto, FVGResult result) {
        FVGRecordDto record = new FVGRecordDto();
        record.setId(symbol.getId());
        record.setName(favoriteSymbolDto.getName());
        record.setTradeDate(result.getTradeDate());
        record.setClose(result.getClose());
        record.setUpAvg(result.getUpAvgValue());
        record.setDownAvg(record.getDownAvg());
        record.setPosition(result.getPosition().name());
        fvgRecordRepository.save(record);
    }

}
