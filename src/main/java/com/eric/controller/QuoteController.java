package com.eric.controller;

import com.eric.domain.Quote;
import com.eric.domain.SymbolType;
import com.eric.domain.SyncResult;
import com.eric.persist.pojo.SymbolDto;
import com.eric.service.QuoteService;
import com.eric.service.SymbolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.eric.domain.SymbolCounter.symbolCnt;
import static com.eric.domain.SymbolCounter.symbolSize;

@Slf4j
@Controller
public class QuoteController {

    @Autowired
    private SymbolService symbolService;
    @Autowired
    private QuoteService quoteService;

    private Future<?> future;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GetMapping("/syncQuote")
    public String getState(Model model) {
        LocalDate today = LocalDate.now();
        if (today.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            today = today.minus(2, ChronoUnit.DAYS);
        }
        if (today.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            today = today.minus(1, ChronoUnit.DAYS);
        }
        List<Quote> quotes = quoteService.getLatestRSIQuotes(today);
        SyncResult result = new SyncResult(symbolCnt, symbolSize, "");
        model.addAttribute("result", result);
        model.addAttribute("quotes", quotes);
        return "syncQuote";
    }


    @PostMapping("/syncQuote")
    public String syncDailyQuote(Model model) {
        List<SymbolDto> tweSymbols = symbolService.getSymbolsFromLocal(SymbolType.TWE);
        symbolSize = tweSymbols.size();
        symbolCnt = 0;
        if (future != null && !future.isDone()) {
            SyncResult result = new SyncResult(symbolCnt, symbolSize, "尚在同步RSI");
            model.addAttribute("result", result);
            return "syncQuote";
        }
        future = executorService.submit(() -> tweSymbols.forEach(symbol -> {
            symbolCnt++;
            log.debug("[{}] {} Sync", symbol.getId(), symbol.getName());
            Quote latestQuote = quoteService.getLatestQuote(symbol.getId());
            LocalDate today = LocalDate.now();
            if (today.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                today = today.minus(2, ChronoUnit.DAYS);
            }
            if (today.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                today = today.minus(1, ChronoUnit.DAYS);
            }

            if (latestQuote == null ||
                    latestQuote.getTradeDate().isBefore(today)) {
                List<Quote> quotes = quoteService.getQuotesFromSite(symbol.getSymbolObj());
                if (quotes == null || quotes.isEmpty()) {
                    return;
                }
                Quote quote = quotes.get(0);
                if (quote != null && quote.getRsi5() < 15) {
                    try {
                        Quote result = quoteService.addQuote(quote);
                        log.info("[{}] {} {} GET", symbol.getId(), symbol.getName(), result.getTradeDate());
                    } catch (Exception e) {
                        log.error("[{}] exception", symbol.getId(), e);
                    }
                }
            }

        }));

//        List<SymbolDto> usSymbols = symbolService.getSymbolsFromLocal(SymbolType.US);
//        usSymbols.forEach(symbol -> {
//            log.debug("[{}] {} Sync",symbol.getId(), symbol.getName());
//            List<Quote> quotes = quoteService.getQuotesFromSite(symbol.getSymbolObj());
//            if (quotes == null || quotes.isEmpty()) {
//                return;
//            }
//            Quote quote = quotes.get(0);
//            if (quote != null && quote.getRsi5() < 15) {
//                log.debug("[{}] {} GET",symbol.getId(), symbol.getName());
//            }
//        });
        SyncResult result = new SyncResult(symbolCnt, symbolSize, "尚在同步RSI");
        model.addAttribute("result", result);
        return "syncQuote";
    }
}
