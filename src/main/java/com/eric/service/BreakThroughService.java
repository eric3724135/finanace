package com.eric.service;

import com.eric.domain.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BreakThroughService {

    public static boolean isBreakThrough(List<Quote> quotes) {
        int firstHighIndex = -1;
        int secondHighIndex = -1;

        // 找到第一個高點
        for (int i = 0; i < quotes.size(); i++) {
            Quote current = quotes.get(i);
            if (firstHighIndex == -1 || current.getHigh() > quotes.get(firstHighIndex).getHigh()) {
                firstHighIndex = i;
            }
        }

        // 找到第二個高點，要求至少間隔5-10日，且低於第一個高點
        for (int i = firstHighIndex + 5; i < quotes.size(); i++) {
            Quote current = quotes.get(i);
            if (current.getHigh() < quotes.get(firstHighIndex).getHigh() &&
                    (secondHighIndex == -1 || current.getHigh() > quotes.get(secondHighIndex).getHigh())) {
                secondHighIndex = i;
            }
        }

        if (firstHighIndex == -1 || secondHighIndex == -1) {
            return false; // 沒有找到合適的兩個高點
        }

        // 計算兩個高點之間的趨勢線 y = mx + b
        double x1 = firstHighIndex;
        double y1 = quotes.get(firstHighIndex).getHigh();
        double x2 = secondHighIndex;
        double y2 = quotes.get(secondHighIndex).getHigh();

        double m = (y2 - y1) / (x2 - x1); // 斜率
        double b = y1 - m * x1; // 截距

        // 檢查是否有某天的收盤價突破趨勢線
        for (int i = secondHighIndex + 1; i < quotes.size(); i++) {
            Quote breakThroughQuote = quotes.get(i);
            double x = i;
            double y = quotes.get(i).getClose();
            double trendLinePrice = m * x + b;

            if (y > trendLinePrice) {
                Quote firstHigh = quotes.get(firstHighIndex);
                Quote secondHigh = quotes.get(secondHighIndex);
                log.info("[{}] {} Date {} ", breakThroughQuote.getSymbol(), breakThroughQuote.getName(), breakThroughQuote.getTradeDate());
                return true;
            }
        }

        return false;
    }
}
