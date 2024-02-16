package com.eric.controller;

import com.eric.domain.Quote;
import com.eric.domain.SymbolCounter;
import com.eric.domain.SyncResult;
import com.eric.excel.USStockExcelReportHandler;
import com.eric.mail.MailConfig;
import com.eric.mail.MailUtils;
import com.eric.service.QuoteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static com.eric.domain.SymbolCounter.*;

@Slf4j
@Controller
public class TweQuoteController {

    @Autowired
    private QuoteService quoteService;
    @Autowired
    private MailConfig mailConfig;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DateTimeFormatter dateFileNameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");


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
            queryDate = queryDate.minusDays(2);
        }
        if (queryDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            queryDate = queryDate.minusDays(1);
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
    @GetMapping("/twe/report")
    public String geneReport(Model model, @RequestParam(required = false, name = "queryDate") String dateStr) {
        LocalDate queryDate;
        if (StringUtils.isBlank(dateStr)) {
            queryDate = LocalDate.now();
            dateStr = queryDate.format(dateFormatter);
        } else {
            queryDate = LocalDate.parse(dateStr, dateFormatter);
        }
        if (queryDate.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            queryDate = queryDate.minusDays(2);
        }
        if (queryDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            queryDate = queryDate.minusDays(1);
        }
        List<Quote> quotes = quoteService.getLatestRSIQuotes(queryDate, Quote.TWE_QUOTE);
        USStockExcelReportHandler handler = new USStockExcelReportHandler();
        try {
            String subject = String.format("%s_台股", queryDate.format(dateFileNameFormatter));
            String filename = subject+".xlsx";
            ByteArrayOutputStream bos = handler.export(quotes);
            MailUtils.generateAndSendEmail(mailConfig, mailConfig.getAddressArr(), subject, subject, filename, bos);
            SyncResult result = new SyncResult(tweSymbolCnt, tweSymbolSize, usSymbolCnt, usSymbolSize, "產檔寄出成功");
            model.addAttribute("result", result);
            model.addAttribute("quotes", new ArrayList<>());
            model.addAttribute("queryDate", dateStr);
            return "twe";
        } catch (Exception e) {
            log.error("[USStockExcelReportHandler] error ", e);
            SyncResult result = new SyncResult(tweSymbolCnt, tweSymbolSize, usSymbolCnt, usSymbolSize, "產檔失敗");
            model.addAttribute("result", result);
            model.addAttribute("quotes", new ArrayList<>());
            model.addAttribute("queryDate", dateStr);
            return "twe";
        }
    }
}
