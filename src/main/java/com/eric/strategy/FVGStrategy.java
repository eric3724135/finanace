package com.eric.strategy;

import com.eric.domain.FVGBox;
import com.eric.domain.Quote;
import com.eric.service.Ta4jIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FVGStrategy {

    @Autowired
    private Ta4jIndicatorService ta4jService;

    /**
     * Determines how many FVGs to consider for calculating the averages.
     * 用于确定在计算平均值时考虑多少个FVG（Fair Value Gaps）。
     */
    private int lookBackNumber = 30;

    /**
     * Bar Count(0) => Uses any FVGs within this Bar Lookback 表示使用特定条数内的FVGs
     * FVG Count(1) => Uses only this amount of recent FVGs 表示只使用最近数量的FVGs
     */
    private int lookBackType = 0;

    /**
     * Only uses FVGs that are greater than ATR * Multiplier.
     * 用于设置ATR（Average True Range）乘数。
     */
    private double atrMulti = 0.25;


    public void execute(String symbol, List<Quote> quotes) {


        ATRIndicator atrIndicator = ta4jService.getATRIndicator(symbol, quotes, 200);
        double atr = atrIndicator.getValue(quotes.size() - 1).doubleValue();
        CircularFifoQueue<FVGBox> upBoxQueue = new CircularFifoQueue<>(lookBackNumber);
        CircularFifoQueue<FVGBox> downBoxQueue = new CircularFifoQueue<>(lookBackNumber);
        List<Double> hstList = new ArrayList<>();
        List<Double> lstList = new ArrayList<>();

        for (int i = 5; i < quotes.size(); i++) {
//            fvg_up = low > high[2] and close[ 1] >high[2] and(low - high[2]) > atr
//            fvg_down = high < low[2] and close[ 1] <low[2] and(low[2] - high) > atr
            boolean fvgUp = quotes.get(i).getLow() > quotes.get(i - 2).getHigh() &&
                    quotes.get(i - 1).getClose() > quotes.get(i - 2).getHigh() &&
                    (quotes.get(i).getLow() - quotes.get(i - 2).getHigh()) > atrIndicator.getValue(i).doubleValue();

            boolean fvgDown = quotes.get(i).getHigh() < quotes.get(i - 2).getLow() &&
                    quotes.get(i - 1).getClose() < quotes.get(i - 2).getLow() &&
                    (quotes.get(i - 2).getLow() - quotes.get(i).getHigh()) > atrIndicator.getValue(i).doubleValue();


//            hst: 计算最近5根K线中的最高值。
//            lst: 计算最近5根K线中的最低值。
            List<Quote> subList = quotes.subList(i - 5, i);
            double hst = 0;
            double lst = 0;
            for (Quote quote : subList) {
                if (hst < quote.getHigh()) {
                    hst = quote.getHigh();
                }
                if (lst > quote.getLow()) {
                    lst = quote.getLow();
                }
            }
            hstList.add(hst);
            lstList.add(lst);
//繪圖ＦＶＧ
            if (fvgUp) {
                upBoxQueue.add(new FVGBox(quotes.get(i), quotes.get(i - 2), quotes.get(i).getLow(), quotes.get(i), quotes.get(i - 2).getHigh()));
            }
            if (fvgDown) {
                downBoxQueue.add(new FVGBox(quotes.get(i), quotes.get(i - 2), quotes.get(i - 2).getLow(), quotes.get(i), quotes.get(i).getHigh()));
            }


            double upValuesSum = 0;
            double upValuesAvg = 0;
            double downValuesSum = 0;
            double downValuesAvg = 0;

            for (FVGBox box : upBoxQueue) {
                upValuesSum += box.getHigh();
            }
            upValuesAvg = upValuesSum/upBoxQueue.size();

            for (FVGBox box : downBoxQueue) {
                downValuesSum += box.getLow();
            }
            downValuesAvg = downValuesSum/downBoxQueue.size();

        }

        log.info("End");

    }
}
