package com.eric.utils;

import java.util.Arrays;
import java.util.List;

public class MathUtils {

    public static double getStandardDeviaction(List<Double> list) {
        double sum = 0;
        double meanValue = mean(list);                //平均數
        for (Double value : list) {
            sum += Math.pow(value - meanValue, 2);
        }
        return Math.sqrt(sum / list.size());
    }

    //求平均值
    public static double mean(List<Double> list) {
        return calcSum(list) / list.size();
    }

    //計算和
    public static double calcSum(List<Double> list) {
        double sum = 0;
        for (Double value : list) {
            sum += value;
        }
        return sum;
    }

    public static void main(String[] args) {
        List<Double> list = Arrays.asList(Double.parseDouble("128.5"),
                Double.parseDouble("130.5"),
                Double.parseDouble("127"),
                Double.parseDouble("128"),
                Double.parseDouble("137"),
                Double.parseDouble("134.5"),
                Double.parseDouble("129"),
                Double.parseDouble("132"),
                Double.parseDouble("135.5"),
                Double.parseDouble("136.5"),
                Double.parseDouble("138"),
                Double.parseDouble("140"),
                Double.parseDouble("145.5"),
                Double.parseDouble("135"),
                Double.parseDouble("136"),
                Double.parseDouble("135"),
                Double.parseDouble("131"),
                Double.parseDouble("133.5"),
                Double.parseDouble("139.5"),
                Double.parseDouble("127"));

        System.out.println(MathUtils.getStandardDeviaction(list));
    }
}
