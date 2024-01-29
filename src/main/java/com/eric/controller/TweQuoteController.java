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
    private QuoteService quoteService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


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

        this.quoteService.scheduleTweDailyQuote();
        SyncResult result = new SyncResult();
        result.setMsg("啟動台股手動更新");
        model.addAttribute("result", result);

        return "admin";

    }
}
