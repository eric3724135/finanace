package com.eric.strategy;

import com.eric.domain.Quote;

import java.util.List;

public class VCPattern {
    // 方法用於檢查是否符合VCP模式`
    public static boolean isVCPattern(List<Quote> quotes) {
        if (quotes.size() < 3) {
            return false; // 至少需要3天的價格列表
        }

        int peakIndex = findPeakIndex(quotes); // 尋找高峰
        int valleyIndex = findValleyIndex(quotes); // 尋找低谷


        if (peakIndex == -1 || valleyIndex == -1) {
            return false; // 如果找不到高峰或低谷，則不符合VCP模式
        }

        // 確保低谷在高峰之前
        if (valleyIndex >= peakIndex) {
            return false;
        }

        // 確保高峰和低谷之間的價格存在明顯的下降和上升趨勢
        double maxPriceBetween = maxPrice(quotes, valleyIndex + 1, peakIndex - 1);
        double minPriceBetween = minPrice(quotes, valleyIndex + 1, peakIndex - 1);

        if (maxPriceBetween <= quotes.get(valleyIndex).getClose() || minPriceBetween >= quotes.get(peakIndex).getClose()) {
            return false;
        }

        return true;
    }

    // 尋找高峰
    private static int findPeakIndex(List<Quote> quotes) {
        for (int i = 1; i < quotes.size() - 1; i++) {
            if (quotes.get(i).getClose() > quotes.get(i - 1).getClose() &&
                    quotes.get(i).getClose() > quotes.get(i + 1).getClose()) {
                return i;
            }
        }
        return -1; // 找不到高峰
    }

    // 尋找低谷
    private static int findValleyIndex(List<Quote> quotes) {
        for (int i = 1; i < quotes.size() - 1; i++) {
            if (quotes.get(i).getClose() < quotes.get(i - 1).getClose() &&
                    quotes.get(i).getClose() < quotes.get(i + 1).getClose()) {
                return i;
            }
        }
        return -1; // 找不到低谷
    }

    // 在指定範圍內找到最大價格
    private static double maxPrice(List<Quote> quotes, int start, int end) {
        double max = Double.MIN_VALUE;
        for (int i = start; i <= end; i++) {
            if (quotes.get(i).getClose() > max) {
                max = quotes.get(i).getClose();
            }
        }
        return max;
    }

    // 在指定範圍內找到最小價格
    private static double minPrice(List<Quote> quotes, int start, int end) {
        double min = Double.MAX_VALUE;
        for (int i = start; i <= end; i++) {
            if (quotes.get(i).getClose() < min) {
                min = quotes.get(i).getClose();
            }
        }
        return min;
    }

}
