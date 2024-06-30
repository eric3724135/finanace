package com.eric.indicator;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.num.Num;

public class VWMAIndicator extends AbstractIndicator<Num> {

    private final int barCount;

    public VWMAIndicator(BarSeries series, int barCount) {
        super(series);
        this.barCount = barCount;
    }

    @Override
    public Num getValue(int index) {
        BarSeries series = getBarSeries();
        int startIndex = Math.max(0, index - barCount + 1);
        Num sumWeightedPrices = series.numOf(0);
        Num sumVolume = series.numOf(0);

        for (int i = startIndex; i <= index; i++) {
            Bar bar = series.getBar(i);
            Num closePrice = bar.getClosePrice();
            Num volume = bar.getVolume();
            sumWeightedPrices = sumWeightedPrices.plus(closePrice.multipliedBy(volume));
            sumVolume = sumVolume.plus(volume);
        }

        return sumVolume.isZero() ? series.numOf(0) : sumWeightedPrices.dividedBy(sumVolume);
    }
}
