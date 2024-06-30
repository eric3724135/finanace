package com.eric.indicator;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.DoubleEMAIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.Num;

public class BayesianTrendIndicator {

    private final BarSeries series;
    private final int length;
    private final int gapLength;
    private final int gap;

    private final Indicator<Num> ema;
    private final Indicator<Num> sma;
    private final Indicator<Num> dema;
    private final Indicator<Num> vwma;

    private final Indicator<Num> emaFast;
    private final Indicator<Num> smaFast;
    private final Indicator<Num> demaFast;
    private final Indicator<Num> vwmaFast;

    public BayesianTrendIndicator(BarSeries series, int length, int gapLength, int gap) {
        this.series = series;
        this.length = length;
        this.gapLength = gapLength;
        this.gap = gap;

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        this.ema = new EMAIndicator(closePrice, length);
        this.sma = new SMAIndicator(closePrice, length);
        this.dema = new DoubleEMAIndicator(closePrice, length);
        this.vwma = new VWMAIndicator(series, length);

        this.emaFast = new EMAIndicator(closePrice, length - gapLength);
        this.smaFast = new SMAIndicator(closePrice, length - gapLength);
        this.demaFast = new DoubleEMAIndicator(closePrice, length - gapLength);
        this.vwmaFast = new VWMAIndicator(series, length - gapLength);
    }

    public Num calculateTrend() {
        Num emaTrendFast = sig(emaFast, gap);
        Num smaTrendFast = sig(smaFast, gap);
        Num demaTrendFast = sig(demaFast, gap);
        Num vwmaTrendFast = sig(vwmaFast, gap);

        Num emaTrend = sig(ema, gap);
        Num smaTrend = sig(sma, gap);
        Num demaTrend = sig(dema, gap);
        Num vwmaTrend = sig(vwma, gap);

        Num priorUp = emaTrend.plus(smaTrend).plus(demaTrend).plus(vwmaTrend).dividedBy(series.numOf(4));
        Num priorDown = series.numOf(1).minus(priorUp);

        Num likelihoodUp = emaTrendFast.plus(smaTrendFast).plus(demaTrendFast).plus(vwmaTrendFast).dividedBy(series.numOf(4));
        Num likelihoodDown = series.numOf(1).minus(likelihoodUp);

        Num posteriorUp = priorUp.multipliedBy(likelihoodUp)
                .dividedBy(priorUp.multipliedBy(likelihoodUp).plus(priorDown.multipliedBy(likelihoodDown)));

        return posteriorUp;
    }

    private Num sig(Indicator<Num> src, int gap) {
        int index = series.getEndIndex();
        if (index < gap) {
            return series.numOf(0);
        }
        return new EMAIndicator(new ConstantIndicator(series, series.numOf(
                src.getValue(index).isGreaterThanOrEqual(src.getValue(index - gap)) ? 1 :
                        src.getValue(index).isGreaterThanOrEqual(src.getValue(index - gap + 1)) ? 0.9 :
                                src.getValue(index).isGreaterThanOrEqual(src.getValue(index - gap + 2)) ? 0.8 :
                                        src.getValue(index).isGreaterThanOrEqual(src.getValue(index - gap + 3)) ? 0.7 :
                                                src.getValue(index).isGreaterThanOrEqual(src.getValue(index - gap + 4)) ? 0.6 :
                                                        src.getValue(index).isGreaterThanOrEqual(src.getValue(index - gap + 5)) ? 0.5 :
                                                                src.getValue(index).isGreaterThanOrEqual(src.getValue(index - gap + 6)) ? 0.4 :
                                                                        src.getValue(index).isGreaterThanOrEqual(src.getValue(index - gap + 7)) ? 0.3 :
                                                                                src.getValue(index).isGreaterThanOrEqual(src.getValue(index - gap + 8)) ? 0.2 :
                                                                                        src.getValue(index).isGreaterThanOrEqual(src.getValue(index - gap + 9)) ? 0.1 :
                                                                                                0)), 4).getValue(index);
    }


}
