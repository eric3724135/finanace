package com.eric;

import com.eric.domain.CMQuote;
import com.eric.domain.Period;
import com.eric.domain.Symbol;
import com.eric.histock.HiStockDataHandler;
import com.eric.parser.ParserResult;
import com.eric.utils.RsiUtils;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        while (true) {
            String stockOption; //1:台股 2:美股
            String symbol;
            String futureType;
            boolean futureParam = true;
            double futureValue = 0;
            Scanner scanner = new Scanner(System.in);
            System.out.print("請輸入查詢對象台股請填1,美股(未實作)請填2：");
            stockOption = scanner.nextLine();
            if (!("1".equals(stockOption) || "2".equals(stockOption))) {
                System.out.println("查詢對象輸入錯誤！！");
                continue;
            }
            System.out.print("請輸入股票代碼：");
            symbol = scanner.nextLine();
            System.out.print("請輸入預測類型,RSI請填1,股價請填2:");
            futureType = scanner.nextLine();
            if (!("1".equals(futureType) || "2".equals(futureType))) {
                System.out.println("預測類型輸入錯誤！！");
                continue;
            }
            if ("1".equals(futureType)) {
                futureParam = true;
                System.out.print("請輸入預測股價推算RSI:");
                futureValue = scanner.nextDouble();
            } else if ("2".equals(futureType)) {
                futureParam = false;
                System.out.print("請輸入預測RSI推算股價:");
                futureValue = scanner.nextDouble();
            }

            //查詢報價
            if ("1".equals(stockOption)) {
                //台股
                HiStockDataHandler parser = new HiStockDataHandler(new Symbol(symbol, ""), Period.ONE_DAY, 10);
                ParserResult<CMQuote> quoteResult = parser.getResult();
                if (quoteResult.getResultList() != null && quoteResult.getResultList().size() > 1) {
                    List<CMQuote> quotes = quoteResult.getResultList();
                    Collections.sort(quotes, (o1, o2) -> o2.getTradeDate().compareTo(o1.getTradeDate()));
                    CMQuote current = quotes.get(0);
                    CMQuote past = quotes.get(1);
                    double result = RsiUtils.calc(6, past.getClose(), current.getClose(), past.getRsi5(), current.getRsi5(), futureValue, futureParam);
                    System.out.println(String.format("股票代碼 %s ", current.getSymbol()));
                    System.out.println(String.format("%s 股價 %s RSI %s", past.getSimpleTradeDateStr(), past.getClose(), String.format("%.2f", past.getRsi5())));
                    System.out.println(String.format("%s 股價 %s RSI %s", current.getSimpleTradeDateStr(), current.getClose(), String.format("%.2f", current.getRsi5())));
                    System.out.println(String.format("預測股價 %s RSI %s",
                            "1".equals(futureType) ? futureValue : String.format("%.2f", result),
                            "1".equals(futureType) ? String.format("%.2f", result) : futureValue));
                }

            } else if ("2".equals(stockOption)) {

            }


        }
    }
}
