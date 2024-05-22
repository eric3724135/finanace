package com.eric.strategy;

import com.eric.domain.FVGBox;
import com.eric.domain.Quote;
import com.eric.service.Ta4jIndicatorService;
import com.eric.utils.ATRCalculator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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

        double test = ATRCalculator.calculateAdjustedATR(quotes, 200, 0.25);
        ATRIndicator atrIndicator = ta4jService.getATRIndicator(symbol, quotes, 200);
        CircularFifoQueue<FVGBox> upBoxQueue = new CircularFifoQueue<>(lookBackNumber);
        CircularFifoQueue<FVGBox> downBoxQueue = new CircularFifoQueue<>(lookBackNumber);
        List<Double> hstList = new ArrayList<>();
        List<Double> lstList = new ArrayList<>();
        Collections.reverse(quotes);
        for (int i = 5; i < quotes.size(); i++) {
            double atr = atrIndicator.getValue(i).doubleValue();
//            Bar bar = atrIndicator.getBarSeries().getBar(i);
            Quote current = quotes.get(i);
//            fvg_up = low > high[2] and close[ 1] >high[2] and(low - high[2]) > atr
//            fvg_down = high < low[2] and close[ 1] <low[2] and(low[2] - high) > atr
            boolean fvgUp = quotes.get(i).getLow() > quotes.get(i - 2).getHigh() &&
                    quotes.get(i - 1).getClose() > quotes.get(i - 2).getHigh() &&
                    (quotes.get(i).getLow() - quotes.get(i - 2).getHigh()) > quotes.get(i).getClose() * 0.005;

            boolean fvgDown = quotes.get(i).getHigh() < quotes.get(i - 2).getLow() &&
                    quotes.get(i - 1).getClose() < quotes.get(i - 2).getLow() &&
                    (quotes.get(i - 2).getLow() - quotes.get(i).getHigh()) > quotes.get(i).getClose() * 0.005;


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
//FVG 找出區塊

            if (fvgUp) {
                upBoxQueue.add(new FVGBox(quotes.get(i), quotes.get(i - 2), quotes.get(i).getLow(), quotes.get(i), quotes.get(i - 2).getHigh()));
                log.info("ADD UP Box {} ", quotes.get(i).getTradeDate());
            }
            if (fvgDown) {
                downBoxQueue.add(new FVGBox(quotes.get(i), quotes.get(i - 2), quotes.get(i - 2).getLow(), quotes.get(i), quotes.get(i).getHigh()));
                log.info("ADD Down Box {} ", quotes.get(i).getTradeDate());
            }

            //Bar Count
            if (lookBackType == 0) {
                // left < bar_index-lb
                upBoxQueue.removeIf(fvgBox -> {
                    int deadLine = quotes.indexOf(current) - lookBackNumber;
                    int check = quotes.indexOf(fvgBox.getLeftQuote());
                    boolean remove = deadLine > check;
                    if (remove) {
                        //log.info("REMOVE Up Box {} deadLine {}  index {}", fvgBox.getRightQuote().getTradeDate(), deadLine, check);
                    }
                    return remove;
                });

                // left < bar_index-lb
                downBoxQueue.removeIf(fvgBox -> {
                    int deadLine = quotes.indexOf(current) - lookBackNumber;
                    int check = quotes.indexOf(fvgBox.getLeftQuote());
                    boolean remove = quotes.indexOf(current) - lookBackNumber > quotes.indexOf(fvgBox.getLeftQuote());
                    if (remove) {
                        log.info("REMOVE Down Box {} deadLine {}  index {}", fvgBox.getRightQuote().getTradeDate(), deadLine, check);

                    }
                    return remove;
                });

            }


            double upValuesSum = 0;
            double upValuesAvg;
            double downValuesSum = 0;
            double downValuesAvg;

            for (FVGBox box : upBoxQueue) {
                upValuesSum += box.getHigh();
            }
            upValuesAvg = upValuesSum / upBoxQueue.size();

            for (FVGBox box : downBoxQueue) {
                downValuesSum += box.getLow();
            }
            downValuesAvg = downValuesSum / downBoxQueue.size();

            log.info("[{}] {} upAvg {} downAvg {} fvgUp {} fvgDown {} upBoxQueue {} downBoxQueue {}", quotes.get(i).getSymbol(), quotes.get(i).getTradeDateStr(), upValuesAvg, downValuesAvg, fvgUp, fvgDown, upBoxQueue.size(), downBoxQueue.size());
            StringBuilder stringBuilder = new StringBuilder();
            for (FVGBox box : downBoxQueue) {
                stringBuilder.append(box.getQuote().getTradeDateStr());
                stringBuilder.append("|");
            }
            log.info("down queue {}",stringBuilder.toString());
        }

        log.info("End");

    }
}
