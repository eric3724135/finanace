package com.eric.utils;

import com.eric.domain.Quote;

import java.util.ArrayList;
import java.util.List;

public class ATRCalculator {

    // 计算指定周期的ATR
    private static double calculateATR(List<Quote> quotes, int length) {
        if (quotes.size() < length ) {
            throw new IllegalArgumentException("Not enough data to calculate ATR.");
        }

        double atr = 0.0;
        for (int i = 1; i < length; i++) {
            double tr = Math.max(quotes.get(i).getHigh() - quotes.get(i).getLow(),
                    Math.max(Math.abs(quotes.get(i).getHigh() - quotes.get(i - 1).getLow()),
                            Math.abs(quotes.get(i).getLow() - quotes.get(i - 1).getHigh())));
            atr += tr;
        }
        return atr / length;
    }

    // 计算累积和
    private static double cumulativeSum(List<Double> data) {
        double sum = 0.0;
        for (double value : data) {
            sum += value;
        }
        return sum;
    }

    // 计算ATR并处理NaN的情况
    public static double calculateAdjustedATR(List<Quote> quotes, int length, double atrMultiplier) {
        double atr;
        try {
            atr = calculateATR(quotes, length) * atrMultiplier;
        } catch (IllegalArgumentException e) {
            atr = Double.NaN;
        }

        if (Double.isNaN(atr)) {
            List<Double> highLowDiff = new ArrayList<>();
            for (int i = 0; i < quotes.size(); i++) {
                highLowDiff.add(quotes.get(i).getHigh() - quotes.get(i).getLow());
            }
            atr = cumulativeSum(highLowDiff) / quotes.size();
        }

        return atr;
    }

//    public static void main(String[] args) {
//        List<Double> high = List.of(1.1, 1.2, 1.3, 1.4, 1.5);
//        List<Double> low = List.of(1.0, 1.1, 1.2, 1.3, 1.4);
//        int length = 5;
//        double atrMultiplier = 0.25;
//
//        double adjustedATR = calculateAdjustedATR(high, low, length, atrMultiplier);
//        System.out.println("Adjusted ATR: " + adjustedATR);
//    }
}

