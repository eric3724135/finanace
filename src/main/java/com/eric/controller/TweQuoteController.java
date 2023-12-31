package com.eric.controller;

import com.eric.domain.Quote;
import com.eric.domain.SymbolCounter;
import com.eric.domain.SymbolType;
import com.eric.domain.SyncResult;
import com.eric.persist.pojo.SymbolDto;
import com.eric.service.QuoteService;
import com.eric.service.SymbolService;
import com.eric.service.Ta4jIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.eric.domain.SymbolCounter.*;

@Slf4j
@Controller
public class TweQuoteController {

    @Autowired
    private SymbolService symbolService;
    @Autowired
    private QuoteService quoteService;

    //    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Future<?> future;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GetMapping("/twe")
    public String getState(Model model, @RequestParam(required = false, name = "queryDate") String dateStr) throws ParseException {
        LocalDate queryDate;
        if (StringUtils.isBlank(dateStr)) {
            queryDate = LocalDate.now();
            dateStr = queryDate.format(dateFormatter);
        } else {
            queryDate = LocalDate.parse(dateStr, dateFormatter);
        }
        if (queryDate.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            queryDate = queryDate.minus(2, ChronoUnit.DAYS);
        }
        if (queryDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            queryDate = queryDate.minus(1, ChronoUnit.DAYS);
        }
        List<Quote> quotes = quoteService.getLatestRSIQuotes(queryDate, Quote.TWE_QUOTE);
        SyncResult result = new SyncResult(SymbolCounter.tweSymbolCnt, SymbolCounter.tweSymbolSize, SymbolCounter.usSymbolCnt, SymbolCounter.usSymbolSize, "");
        model.addAttribute("result", result);
        model.addAttribute("quotes", quotes);
        model.addAttribute("queryDate", dateStr);
        return "twe";
    }


    @PostMapping("/twe")
    public String syncDailyQuote(Model model) {

        List<SymbolDto> tweSymbols = symbolService.getSymbolsFromLocal(SymbolType.TWE);
        SymbolCounter.tweSymbolSize = tweSymbols.size();
        SymbolCounter.tweSymbolCnt = 0;
        if (future != null && !future.isDone()) {
            SyncResult result = new SyncResult(SymbolCounter.tweSymbolCnt, SymbolCounter.tweSymbolSize, SymbolCounter.usSymbolCnt, SymbolCounter.usSymbolSize, "尚在同步RSI");
            model.addAttribute("result", result);
            model.addAttribute("quotes", new ArrayList<>());
            return "twe";
        }
        future = executorService.submit(() -> {
            tweSymbols.forEach(symbol -> {
                tweSymbolCnt++;
                log.debug("[{}] {} Sync", symbol.getId(), symbol.getName());
                Quote latestQuote = quoteService.getLatestQuote(symbol.getId());
                LocalDate today = LocalDate.now();
                if (today.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                    today = today.minus(2, ChronoUnit.DAYS);
                }
                if (today.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                    today = today.minus(1, ChronoUnit.DAYS);
                }

                if (latestQuote == null
                        || latestQuote.getTradeDate().isBefore(today)
                        || latestQuote.getTradeDate().isEqual(today)) {
                    List<Quote> quotes = quoteService.getTweQuotesFromSite(symbol.getSymbolObj());
                    if (quotes == null || quotes.isEmpty()) {
                        return;
                    }
                    Quote quote = quotes.get(0);
                    if (quote != null && quote.getRsi5() < 20) {
                        try {
                            boolean isExist = quoteService.getQuoteExist(quote.getSymbol(), quote.getTradeDate());
                            if (!isExist) {
                                Quote result = quoteService.addQuote(quote);
                                log.info("[{}] {} {} GET", symbol.getId(), symbol.getName(), result.getTradeDate());
                            }
                        } catch (Exception e) {
                            log.error("[{}] exception", symbol.getId(), e);
                        }
                    }
                }

            });

        });


        SyncResult result = new SyncResult(SymbolCounter.tweSymbolCnt, SymbolCounter.tweSymbolSize, SymbolCounter.usSymbolCnt, SymbolCounter.usSymbolSize, "尚在同步RSI");
        model.addAttribute("result", result);
        model.addAttribute("quotes", new ArrayList<>());
        return "twe";
    }
}
