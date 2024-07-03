package com.eric.service;

import com.eric.domain.*;
import com.eric.excel.FVGStrategyExcelHandler;
import com.eric.indicator.BayesianTrendIndicator;
import com.eric.mail.MailConfig;
import com.eric.mail.MailUtils;
import com.eric.persist.pojo.FVGRecordDto;
import com.eric.persist.pojo.FavoriteSymbolDto;
import com.eric.persist.pojo.ProfitDto;
import com.eric.persist.repo.FVGRecordRepository;
import com.eric.persist.repo.FavoriteSymbolRepository;
import com.eric.persist.repo.ProfitRepository;
import com.eric.strategy.FVGStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.num.Num;

import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
public class FVGService {


    @Autowired
    private SymbolService symbolService;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private FavoriteSymbolRepository favoriteSymbolRepository;
    @Autowired
    private Ta4jIndicatorService ta4jIndicatorService;
    @Autowired
    private FVGStrategy strategy;
    @Autowired
    private MailConfig mailConfig;

    private Future<?> tweFuture;
    private Future<?> usFuture;

    @Autowired
    private FVGRecordRepository fvgRecordRepository;
    @Autowired
    private ProfitRepository profitRepository;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);


    public List<FVGRecordDto> findRecordByRangeAndType(LocalDate startDate, LocalDate endDate, SymbolType symbolType) {
        List<FVGRecordDto> results = new ArrayList<>();
        switch (symbolType) {
            case TWE -> {
                results = fvgRecordRepository.findTweRecordByRange(startDate, endDate);
            }
            case US -> {
                results = fvgRecordRepository.findUsRecordByRange(startDate, endDate);
            }

        }
        return results;
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
            if (quote.getLow() < object.getLowestQuote().getLow()) {
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

        String oriId = object.getId().split("\\.")[0];
        Optional<FavoriteSymbolDto> optionalSymbol = favoriteSymbolRepository.findById(oriId);
        if (optionalSymbol.isPresent()) {
            FavoriteSymbolDto symbolDto = optionalSymbol.get();
            object.setCategory(symbolDto.getCategory());
        }
        //get ma 排序狀況
        this.fetchMaSorting(object, oriQuotes);
        //Bayesian Trend Indicator
        this.fetchBayesianTrendIndicator(object, oriQuotes);
        return object;
    }

    @Scheduled(cron = "0 0 16 * * ?")
    public void scheduleTweFVGStrategy() {
        if (tweFuture != null && !tweFuture.isDone()) {
            return;
        }
        tweFuture = executorService.submit(() -> {


            this.fetchTweFVGStrategy();
            List<FVGRecordDto> list = this.findRecordByRangeAndType(LocalDate.now(), LocalDate.now(), SymbolType.TWE);
//            List<FVGObject> analysisList = new ArrayList<>();
//            for (FVGRecordDto recordDto : list) {
//                FVGObject object = this.analysis(recordDto);
//                analysisList.add(object);
//            }
            FVGStrategyExcelHandler handler = new FVGStrategyExcelHandler();
            try {
                ByteArrayOutputStream bos = handler.export(list);
                MailUtils.generateAndSendEmail(mailConfig, mailConfig.getAddressArr(),
                        String.format("%s_台股FVG每日挑檔", LocalDate.now().format(dateFormatter)),
                        String.format("%s_台股FVG每日挑檔", LocalDate.now().format(dateFormatter)),
                        String.format("%s_台股FVG.xlsx", LocalDate.now().format(dateFormatter)), bos);
            } catch (IOException | MessagingException e) {
                log.error("[FVGStrategyExcelHandler] error ", e);
            }
        });


    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void scheduleUsFVGStrategy() {
        if (usFuture != null && !usFuture.isDone()) {
            return;
        }
        usFuture = executorService.submit(() -> {
            this.fetchUsFVGStrategy();
            List<FVGRecordDto> list = this.findRecordByRangeAndType(LocalDate.now(), LocalDate.now(), SymbolType.US);
//        List<FVGObject> analysisList = new ArrayList<>();
//        for (FVGRecordDto recordDto : list) {
//            FVGObject object = this.analysis(recordDto);
//            analysisList.add(object);
//        }
            FVGStrategyExcelHandler handler = new FVGStrategyExcelHandler();
            try {
                ByteArrayOutputStream bos = handler.export(list);
                MailUtils.generateAndSendEmail(mailConfig, mailConfig.getAddressArr(),
                        String.format("%s_美股FVG每日挑檔", LocalDate.now().format(dateFormatter)),
                        String.format("%s_美股FVG每日挑檔", LocalDate.now().format(dateFormatter)),
                        String.format("%s_美股FVG.xlsx", LocalDate.now().format(dateFormatter)), bos);
            } catch (IOException | MessagingException e) {
                log.error("[FVGStrategyExcelHandler] error ", e);
            }
        });

    }


    public void fetchTweFVGStrategy() {
        log.info("TWE FVG 策略啟動");

        List<FavoriteSymbolDto> tweList = symbolService.getFavoriteSymbols(SymbolType.TWE);


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
            List<FVGResult> buySellList = new ArrayList<>();
//            FVGResult result = results.get(results.size() - 1);
            for (int i = 0; i < results.size(); i++) {
                FVGResult result = results.get(i);
                //後續計算prodit用
                if (FVGPosition.BUY.equals(result.getPosition()) || FVGPosition.SELL.equals(result.getPosition())) {
                    buySellList.add(result);
                }
                if (i >= results.size() - 100) {
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

            ProfitDto profit = null;
            for (FVGResult result : buySellList) {
                if (FVGPosition.BUY.equals(result.getPosition())) {
                    profit = new ProfitDto(favoriteSymbolDto.getId(), favoriteSymbolDto.getName(), "FVG_TWE", LocalDate.now());
                    profit.setBuyDate(result.getTradeDate());
                    profit.setBuyPrice(result.getClose());
                }
                if (FVGPosition.SELL.equals(result.getPosition())) {
                    assert profit != null;
                    profit.setSellDate(result.getTradeDate());
                    profit.setSellPrice(result.getClose());
                    profit.setProfit(profit.getSellPrice() / profit.getBuyPrice() - 1);
                    if (profit.getBuyDate().isAfter(LocalDate.of(2022, 1, 1))) {
                        ProfitDto exists = profitRepository.findBySymbolAndBuyDate(profit.getId(), profit.getStrategy(), profit.getBuyDate());
                        if (exists == null) {
                            profitRepository.save(profit);
                        }
                    }
                    profit = null;

                }
            }
        }

    }

    public void fetchUsFVGStrategy() {
        log.info("US FVG 策略啟動");

        List<FavoriteSymbolDto> usList = symbolService.getFavoriteSymbols(SymbolType.US);

        for (FavoriteSymbolDto favoriteSymbolDto : usList) {
            Symbol symbol = favoriteSymbolDto.getSymbolObj();
            List<Quote> quotes = quoteService.getusQuotesFromSite(symbol, "1d", "10y");
            if (quotes == null || quotes.size() < 10) {
                continue;
            }
            List<FVGResult> results = strategy.execute(symbol.getId(), quotes);
            List<FVGResult> buySellList = new ArrayList<>();
//            FVGResult result = results.get(results.size() - 1);
            for (int i = 0; i < results.size(); i++) {
                FVGResult result = results.get(i);
                //後續計算prodit用
                if (FVGPosition.BUY.equals(result.getPosition()) || FVGPosition.SELL.equals(result.getPosition())) {
                    buySellList.add(result);
                }
                if (i >= results.size() - 100) {

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
            ProfitDto profit = null;
            for (FVGResult result : buySellList) {
                if (FVGPosition.BUY.equals(result.getPosition())) {
                    profit = new ProfitDto(favoriteSymbolDto.getId(), favoriteSymbolDto.getName(), "FVG_US", LocalDate.now());
                    profit.setBuyDate(result.getTradeDate());
                    profit.setBuyPrice(result.getClose());
                }
                if (FVGPosition.SELL.equals(result.getPosition())) {
                    assert profit != null;
                    profit.setSellDate(result.getTradeDate());
                    profit.setSellPrice(result.getClose());
                    profit.setProfit(profit.getSellPrice() / profit.getBuyPrice() - 1);
                    if (profit.getBuyDate().isAfter(LocalDate.of(2022, 1, 1))) {
                        ProfitDto exists = profitRepository.findBySymbolAndBuyDate(profit.getId(), profit.getStrategy(), profit.getBuyDate());
                        if (exists == null) {
                            profitRepository.save(profit);
                        }
                    }
                    profit = null;

                }
            }
        }

    }

    public List<FVGRecordDto> findTweRecordStillHold() {
        return fvgRecordRepository.findTweStillHoldBuy();
    }

    public List<FVGRecordDto> findUsRecordStillHold() {
        return fvgRecordRepository.findUsStillHoldBuy();
    }

    public FVGObject fetchMaSorting(FVGObject object, List<Quote> quotes) {
        SMAIndicator sma5Indicator = ta4jIndicatorService.getSMAIndicator(object.getId(), quotes, 5);
        SMAIndicator sma10Indicator = ta4jIndicatorService.getSMAIndicator(object.getId(), quotes, 10);
        SMAIndicator sma20Indicator = ta4jIndicatorService.getSMAIndicator(object.getId(), quotes, 20);
        SMAIndicator sma60Indicator = ta4jIndicatorService.getSMAIndicator(object.getId(), quotes, 60);
        double ma5 = sma5Indicator.getValue(sma5Indicator.getBarSeries().getEndIndex()).doubleValue();
        double ma10 = sma10Indicator.getValue(sma5Indicator.getBarSeries().getEndIndex()).doubleValue();
        double ma20 = sma20Indicator.getValue(sma5Indicator.getBarSeries().getEndIndex()).doubleValue();
        double ma60 = sma60Indicator.getValue(sma5Indicator.getBarSeries().getEndIndex()).doubleValue();
        TreeMap<Double, String> map = new TreeMap<>(Collections.reverseOrder());
        map.put(ma5, "ma5");
        map.put(ma10, "ma10");
        map.put(ma20, "ma20");
        map.put(ma60, "ma60");
        StringBuilder builder = new StringBuilder();
        map.forEach((value, str) -> builder.append(str + ">"));
        object.setMaSorting(builder.toString());
        return object;
    }

    public FVGObject fetchBayesianTrendIndicator(FVGObject object, List<Quote> quotes) {

        BarSeries series = ta4jIndicatorService.transfer(object.getId(), quotes); // Load your BarSeries data here
        BayesianTrendIndicator indicator = new BayesianTrendIndicator(series, 60, 20, 10);
        Num trend = indicator.calculateTrend();
        object.setBayesianTrend(trend.doubleValue());
        return object;
    }

    public void getProfitReport() {

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
