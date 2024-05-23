package com.eric.controller;

import com.eric.domain.*;
import com.eric.mail.MailConfig;
import com.eric.persist.pojo.FavoriteSymbolDto;
import com.eric.service.*;
import com.eric.strategy.FVGStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;
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
    private AnalysisService analysisService;
    @Autowired
    private WessiorFintechService wessiorFintechService;

    @Autowired
    private MailConfig mailConfig;
    @Autowired
    private FVGStrategy fvgStrategy;

    private void setDefaultModel(Model model) {
        SyncResult result = new SyncResult();
        model.addAttribute("result", result);
        model.addAttribute("symbol", new Symbol());
    }

    @GetMapping("/admin")
    public String quoteList(Model model) {
        this.setDefaultModel(model);
        return "admin";
    }

    @PostMapping("/truncateQuote")
    public String truncateQuote(Model model) {
        this.setDefaultModel(model);
        SyncResult result = (SyncResult) model.getAttribute("result");
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
        this.setDefaultModel(model);
        SyncResult result = (SyncResult) model.getAttribute("result");
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

    /**
     * @param model
     * @return
     */
    @PostMapping("/favorite")
    public String favorite(Model model, @ModelAttribute("symbol") Symbol symbol) {
        this.setDefaultModel(model);
        SyncResult result = (SyncResult) model.getAttribute("result");
        try {
            FavoriteSymbolDto symbolDto = symbolService.addFavoriteSymbol(symbol.getId());

            if (symbolDto == null) {
                result.setMsg("查無股票代碼");
            } else {
                result.setMsg(String.format("新增成功 [%s] %s", symbolDto.getId(), symbolDto.getName()));
            }
            model.addAttribute("result", result);
        } catch (Exception e) {
            result.setMsg(String.format("新增關注股票失敗 %s", e.getMessage()));
            model.addAttribute("result", result);
        }

        return "admin";
    }


    @PostMapping("/test")
    public String test(Model model) {

//        List<SymbolDto> usSymbols = symbolService.getSymbolsFromLocal(SymbolType.US);
//        usSymbols.forEach(usSymbol -> {
//            try {
//                List<Quote> quotes = quoteService.getusQuotesFromSite(usSymbol.getSymbolObj(), "1d", "6mo");
//                if (quotes == null || quotes.isEmpty()) {
//                    return;
//                }
//                //boolean isWWay = strategyService.analysisWWayStrategy(quotes);
////                if (isWWay) {
////                    log.info("[{}] w way", usSymbol.getId());
////                }
//                boolean isVCPattern = VCPattern.isVCPattern(quotes);
//                if (isVCPattern) {
//                    log.info("[{}] is VCPattern", usSymbol.getId());
//                }
//            } catch (Exception e) {
//                log.error("[{}] error", usSymbol.getId());
//            }
//
//        });
        Symbol symbol = new Symbol("2330.tw", "tsmc");
        symbol.setType(SymbolType.US);
        List<Quote> quotes = quoteService.getusQuotesFromSite(symbol, "1d", "6mo");
//        this.analysisService.handleRSI(symbol, quotes, 6);
//        List<Quote> weekQuotes = quoteService.getusQuotesFromSite(symbol, "1wk", "1y");
//        this.analysisService.handleRSI(symbol, weekQuotes, 6);
        List<FVGResult> results = fvgStrategy.execute(symbol.getId(), quotes);
        Collections.reverse(results);
        //頁面必須回傳值
        this.setDefaultModel(model);
        SyncResult result = (SyncResult) model.getAttribute("result");
        result.setMsg("test done");
        model.addAttribute("result", result);
        return "admin";
    }


}
