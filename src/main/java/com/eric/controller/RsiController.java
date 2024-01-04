package com.eric.controller;

import com.eric.domain.Quote;
import com.eric.domain.Period;
import com.eric.domain.RsiResult;
import com.eric.domain.Symbol;
import com.eric.histock.HiStockDataHandler;
import com.eric.parser.ParserResult;
import com.eric.service.AnalysisService;
import com.eric.service.QuoteService;
import com.eric.utils.RsiUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class RsiController {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private AnalysisService analysisService;


    @GetMapping("/rsi")
    public String rsiCalcPage(Model model) {
        RsiResult result = new RsiResult();
        model.addAttribute("rsiResult", result);

        return "rsi";
    }

    @PostMapping("/rsi")
    public String submitForm(Model model, @ModelAttribute("rsiResult") RsiResult result) {
        if (!("1".equals(result.getStockType()) || "2".equals(result.getStockType()))) {
            System.out.println("查詢對象輸入錯誤！！");
            return "rsi";
        }
        if (!("1".equals(result.getFutureType()) || "2".equals(result.getFutureType()))) {
            System.out.println("預測類型輸入錯誤！！");
            return "rsi";
        }
        if ("1".equals(result.getStockType())) {
            //台股
            HiStockDataHandler parser = new HiStockDataHandler(new Symbol(result.getSymbol(), ""), Period.ONE_DAY, 10);
            ParserResult<Quote> quoteResult = parser.getResult();
            if (quoteResult.getResultList() != null && quoteResult.getResultList().size() > 1) {
                List<Quote> quotes = quoteResult.getResultList();
                Collections.sort(quotes, (o1, o2) -> o2.getTradeDate().compareTo(o1.getTradeDate()));
                Quote current = quotes.get(0);
                Quote past = quotes.get(1);
                double resultValue = RsiUtils.calc(6, past.getClose(), current.getClose(), past.getRsi5(), current.getRsi5(), result.getFutureParam(), "1".equals(result.getFutureType()));

                result.setPast(past);
                result.setCurrent(current);
                result.setFutureResult(resultValue);

                System.out.println(String.format("股票代碼 %s ", current.getSymbol()));
                System.out.println(String.format("%s 股價 %s RSI %s", past.getSimpleTradeDateStr(), past.getClose(), String.format("%.2f", past.getRsi5())));
                System.out.println(String.format("%s 股價 %s RSI %s", current.getSimpleTradeDateStr(), current.getClose(), String.format("%.2f", current.getRsi5())));
                System.out.println(String.format("預測股價 %s RSI %s",
                        "1".equals(result.getFutureType()) ? result.getFutureParam() : String.format("%.2f", resultValue),
                        "1".equals(result.getFutureType()) ? String.format("%.2f", resultValue) : result.getFutureParam()));

            }

        } else if ("2".equals(result.getStockType())) {

        }

        model.addAttribute("rsiResult", result);

        return "rsi";
    }

//    @GetMapping("/quotes")
//    @ResponseBody
//    public List<Quote> showQuotes(@RequestParam String symbol) {
//        List<Quote> quotes = quoteService.getTweQuotesFromSite(new Symbol(symbol, ""));
//
//        return quotes;
//    }

    @GetMapping("/quote")
    public String quoteList(Model model) {
        Symbol symbol = new Symbol();
        model.addAttribute("symbol", symbol);
        model.addAttribute("quotes", new ArrayList<>());
        return "quote";
    }

    @PostMapping("/quote")
    public String quoteSearch(Model model, @ModelAttribute("symbol") Symbol symbol) {
        List<Quote> quotes = quoteService.getTweQuotesFromSite(symbol);
        if (quotes == null || quotes.isEmpty()) {
            quotes = quoteService.getusQuotesFromSite(symbol);
            quotes = analysisService.handleRSI(symbol, quotes, 6);
        }
        List<Quote> result = quotes.subList(0, quotes.size() > 30 ? 30 : quotes.size() - 1);
        model.addAttribute("quotes", result);
        return "quote";
    }
}
