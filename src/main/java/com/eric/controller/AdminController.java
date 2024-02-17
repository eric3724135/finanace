package com.eric.controller;

import com.eric.domain.*;
import com.eric.mail.MailConfig;
import com.eric.persist.pojo.SymbolDto;
import com.eric.service.AdminService;
import com.eric.service.QuoteService;
import com.eric.service.StrategyService;
import com.eric.service.SymbolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.mail.MessagingException;
import java.util.List;


@Slf4j
@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private SymbolService symbolService;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private StrategyService strategyService;

    @Autowired
    private MailConfig mailConfig;

    @GetMapping("/admin")
    public String quoteList(Model model) {
        SyncResult result = new SyncResult();
        model.addAttribute("result", result);
        return "admin";
    }

    @PostMapping("/truncateQuote")
    public String truncateQuote(Model model) {
        SyncResult result = new SyncResult();
        try {
            adminService.truncateQuote();
            result.setMsg("已清空報價資料庫");
            model.addAttribute("result", result);
        } catch (Exception e) {
            result.setMsg(String.format("報價資料庫清空失敗 %s", e.getMessage()));
            model.addAttribute("result", result);
        }

        return "admin";
    }

    @PostMapping("/truncateSymbol")
    public String truncateSymbol(Model model) {
        SyncResult result = new SyncResult();
        try {
            adminService.truncateSymbol();
            result.setMsg("已清空標的資料庫");
            model.addAttribute("result", result);
        } catch (Exception e) {
            result.setMsg(String.format("標的資料庫清空失敗 %s", e.getMessage()));
            model.addAttribute("result", result);
        }

        return "admin";
    }

    @PostMapping("/test")
    public String test(Model model) {

        List<SymbolDto> usSymbols = symbolService.getSymbolsFromLocal(SymbolType.US);
        usSymbols.forEach(usSymbol -> {
            try {
                List<Quote> quotes = quoteService.getusQuotesFromSite(usSymbol.getSymbolObj(), "1d", "6mo");
                if (quotes == null || quotes.isEmpty()) {
                    return;
                }
                boolean isWWay = strategyService.analysisWWayStrategy(quotes);
                if (isWWay) {
                    log.info("[{}] w way", usSymbol.getId());
                }
            } catch (Exception e) {
                log.error("[{}] error", usSymbol.getId());
            }

        });
        //頁面必須回傳值
        SyncResult result = new SyncResult();
        result.setMsg("test done");
        model.addAttribute("result", result);
        return "admin";
    }


}
