package com.eric.utils;

public class RsiUtils {

    /**
     * n p1 p2 r1 r2 r3p3 boolean
     */
    public static double calc(double n, double p1, double p2, double r1, double r2, double r3p3, boolean returnRsi) {
        double result;
        double w = (n - 1) / n; //<------- Exponential decay weighting of RSI

        double rs1 = r1 / 100;
        double rs2 = r2 / 100;
        double g;
        if (p2 - p1 >= 0) {
            g = (p2 - p1) * (1 - rs2) / ((rs2 / rs1 - 1) * w);
        } else {
            g = rs2 * (p1 - p2) / (1 - rs2 / rs1) / w;
        }
        double l = (g / rs1) - g;

        if (returnRsi) {
            //return rsi
            if ((r3p3 - p2) >= 0 && (p2 - p1) >= 0) {
                double resultRsi = ((((g * w) + (p2 - p1)) * w) + (r3p3 - p2)) / (((((g + l) * w) + (p2 - p1)) * w) + (r3p3 - p2));
                result = resultRsi * 100;
            } else if ((r3p3 - p2) >= 0 && (p2 - p1) < 0) {
                double resultRsi = (((g * w) * w) + (r3p3 - p2)) / (((((g + l) * w) - (p2 - p1)) * w) + (r3p3 - p2));
                result = resultRsi * 100;
            } else if ((r3p3 - p2) < 0 && (p2 - p1) >= 0) {
                double resultRsi = (((g * w) + (p2 - p1)) * w) / (((((g + l) * w) + (p2 - p1)) * w) - (r3p3 - p2));
                result = resultRsi * 100;
            } else {
                double resultRsi = ((g * w) * w) / ((((g + l) * w) - (p2 - p1)) * w - (r3p3 - p2));
                result = resultRsi * 100;
            }
        } else {
            //return price
            double resultRsi = r3p3 / 100;
            if ((resultRsi - rs2) >= 0 && (p2 - p1) >= 0) {
                result = p2 + ((((g * w) + (p2 - p1)) * w) - (resultRsi * ((((g + l) * w) + (p2 - p1)) * w))) / (resultRsi - 1);
            } else if ((resultRsi - rs2) >= 0 && (p2 - p1) < 0) {
                result = p2 + (((g * w) * w) - (resultRsi * ((((g + l) * w) - (p2 - p1)) * w))) / (resultRsi - 1);
            } else if ((resultRsi - rs2) < 0 && (p2 - p1) >= 0) {
                result = p2 + resultRsi * (((((g + l) * w) + (p2 - p1)) * w) - (((g * w) + (p2 - p1)) * w)) / resultRsi;
            } else {
                result = p2 + (resultRsi * ((((g + l) * w) - (p2 - p1)) * w) - ((g * w) * w)) / resultRsi;
            }
        }
        return result;
    }
}
