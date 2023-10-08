package com.eric.controller;

import com.eric.domain.CMQuote;
import com.eric.domain.Period;
import com.eric.domain.RsiResult;
import com.eric.domain.Symbol;
import com.eric.histock.HiStockDataHandler;
import com.eric.parser.ParserResult;
import com.eric.utils.RsiUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;
import java.util.List;

@Controller
public class RsiController {


    @GetMapping("/")
    public String rsiCalcPage(Model model) {
        RsiResult result = new RsiResult();
        model.addAttribute("rsiResult", result);

        return "index";
    }

    @PostMapping("/")
    public String submitForm(Model model, @ModelAttribute("rsiResult") RsiResult result) {
        if (!("1".equals(result.getStockType()) || "2".equals(result.getStockType()))) {
            System.out.println("查詢對象輸入錯誤！！");
            return "index";
        }
        if (!("1".equals(result.getFutureType()) || "2".equals(result.getFutureType()))) {
            System.out.println("預測類型輸入錯誤！！");
            return "index";
        }
        if ("1".equals(result.getStockType())) {
                //台股
                HiStockDataHandler parser = new HiStockDataHandler(new Symbol(result.getSymbol(), ""), Period.ONE_DAY, 10);
                ParserResult<CMQuote> quoteResult = parser.getResult();
                if (quoteResult.getResultList() != null && quoteResult.getResultList().size() > 1) {
                    List<CMQuote> quotes = quoteResult.getResultList();
                    Collections.sort(quotes, (o1, o2) -> o2.getTradeDate().compareTo(o1.getTradeDate()));
                    CMQuote current = quotes.get(0);
                    CMQuote past = quotes.get(1);
                    double resultValue = RsiUtils.calc(6, past.getClose(), current.getClose(), past.getRsi5(), current.getRsi5(), result.getFutureParam(), "1".equals(result.getFutureType()));

                    result.setPast(past);
                    result.setCurrent(current);
                    result.setFutureResult(resultValue);

                    System.out.println(String.format("股票代碼 %s ", current.getSymbol()));
                    System.out.println(String.format("%s 股價 %s RSI %s", past.getSimpleTradeDateStr(), past.getClose(), String.format("%.2f", past.getRsi5())));
                    System.out.println(String.format("%s 股價 %s RSI %s", current.getSimpleTradeDateStr(), current.getClose(), String.format("%.2f", current.getRsi5())));
                    System.out.println(String.format("預測股價 %s RSI %s",
                            "1".equals(result.getFutureType()) ? result.getFutureParam() : String.format("%.2f", resultValue),
                            "1".equals(result.getFutureType()) ? String.format("%.2f", resultValue) : result.getFutureParam() ));

                }

            } else if ("2".equals(result.getStockType())) {

            }

        model.addAttribute("rsiResult", result);

        return "index";
    }
}
