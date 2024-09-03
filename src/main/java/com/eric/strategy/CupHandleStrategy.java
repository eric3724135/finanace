package com.eric.strategy;

import com.eric.domain.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Component
public class CupHandleStrategy {

    public List<Quote> findCupAndHandlePatterns(List<Quote> quotes) {
        List<Quote> patterns = new ArrayList<>();

        int n = quotes.size();
        if (n < 30) return patterns; // 数据量不足以形成杯柄形态

        int minCupPeriod = 30;
        int maxCupPeriod = 120;
        double minCupDepth = 0.12;
        double maxCupDepth = 0.30;

        for (int start = 0; start < n - minCupPeriod; start++) {
            for (int end = start + minCupPeriod; end < Math.min(n, start + maxCupPeriod); end++) {
                List<Quote> cupAndHandle = quotes.subList(start, end + 1);

                if (isCupAndHandlePattern(cupAndHandle)) {
                    patterns.add(cupAndHandle.get(0));
                    start = end; // 跳过已经确认的形态，以避免重叠
                    break;
                }
            }
        }
        return patterns;
    }

    private boolean isCupAndHandlePattern(List<Quote> quotes) {
        int n = quotes.size();

        int cupStart = -1, cupEnd = -1;
        double highestPrice = 0;
        double lowestPrice = Double.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            double high = quotes.get(i).getHigh();
            double low = quotes.get(i).getLow();

            if (high > highestPrice) {
                highestPrice = high;
                cupStart = i;
            }

            if (low < lowestPrice) {
                lowestPrice = low;
                cupEnd = i;
            }

            if (cupStart != -1 && cupEnd != -1 && cupEnd > cupStart) {
                double cupDepth = (highestPrice - lowestPrice) / highestPrice;
                if (cupDepth >= 0.12 && cupDepth <= 0.30) {
                    break;
                } else {
                    cupStart = -1;
                    cupEnd = -1;
                }
            }
        }

        if (cupStart == -1 || cupEnd == -1 || (cupEnd - cupStart + 1) < 30 || (cupEnd - cupStart + 1) > 120) {
            return false;
        }

        int handleStart = cupEnd + 1;
        if (handleStart >= n) return false;

        double handleHighestPrice = quotes.get(handleStart).getHigh();
        boolean isVolumeDecreasing = true;

        for (int i = handleStart; i < n; i++) {
            double high = quotes.get(i).getHigh();
            double low = quotes.get(i).getLow();
            double volume = quotes.get(i).getVolume();

            if (high > handleHighestPrice) {
                handleHighestPrice = high;
            }

            if (high > highestPrice || low < lowestPrice) {
                return false;
            }

            if (i > handleStart && volume > quotes.get(i - 1).getVolume()) {
                isVolumeDecreasing = false;
            }
        }

        if (!isVolumeDecreasing) return false;

        return true;
    }
}
