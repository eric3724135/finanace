package com.eric.strategy;

import com.eric.domain.FVGBox;
import com.eric.domain.FVGPosition;
import com.eric.domain.FVGResult;
import com.eric.domain.Quote;
import com.eric.service.Ta4jIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ta4j.core.indicators.ATRIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class FVGStrategy {

    @Autowired
    private Ta4jIndicatorService ta4jService;

    private static final int DEFAULT_LOOK_BACK_NUMBER = 30;

    private static final int DEFAULT_LOOK_BACK_TYPE = 0;

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

    /**
     * lookBackType 計算資料方式 FVG 數量 bar數量
     * lookBackNumber 回推計算多少筆資料
     * @param symbol symbol
     * @param quotes quotes
     * @return List<FVGResult>
     */
    public List<FVGResult> execute(String symbol, List<Quote> quotes, int lookBackNumber, int lookBackType) {
        this.lookBackType = lookBackType;
        this.lookBackNumber = lookBackNumber;
        return this.executeMain(symbol, quotes);
    }

    /**
     * lookBackType = 0(Bar Count) default
     * lookBackNumber = 30 default
     *
     * @param symbol symbol
     * @param quotes quotes
     * @return List<FVGResult>
     */
    public List<FVGResult> execute(String symbol, List<Quote> quotes) {
        this.lookBackType = DEFAULT_LOOK_BACK_TYPE;
        this.lookBackNumber = DEFAULT_LOOK_BACK_NUMBER;
        return this.executeMain(symbol, quotes);
    }

    public List<FVGResult> executeMain(String symbol, List<Quote> quotes) {
        List<FVGResult> results = new ArrayList<>();
        ATRIndicator atrIndicator = ta4jService.getATRIndicator(symbol, quotes, 200);
        CircularFifoQueue<FVGBox> upBoxQueue = new CircularFifoQueue<>(lookBackNumber);
        CircularFifoQueue<FVGBox> downBoxQueue = new CircularFifoQueue<>(lookBackNumber);
        Collections.reverse(quotes);
        for (int i = 5; i < quotes.size(); i++) {
            double atr = atrIndicator.getValue(i).doubleValue();
            Quote current = quotes.get(i);
            boolean fvgUp = quotes.get(i).getLow() > quotes.get(i - 2).getHigh() &&
                    quotes.get(i - 1).getClose() > quotes.get(i - 2).getHigh() &&
                    (quotes.get(i).getLow() - quotes.get(i - 2).getHigh()) > atr * atrMulti;

            boolean fvgDown = quotes.get(i).getHigh() < quotes.get(i - 2).getLow() &&
                    quotes.get(i - 1).getClose() < quotes.get(i - 2).getLow() &&
                    (quotes.get(i - 2).getLow() - quotes.get(i).getHigh()) > atr * atrMulti;


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
            //FVG 找出區塊

            if (fvgUp) {
                upBoxQueue.add(new FVGBox(quotes.get(i), quotes.get(i - 2), quotes.get(i).getLow(), quotes.get(i), quotes.get(i - 2).getHigh()));
                log.debug("ADD UP Box {} ", quotes.get(i).getTradeDate());
            }
            if (fvgDown) {
                downBoxQueue.add(new FVGBox(quotes.get(i), quotes.get(i - 2), quotes.get(i - 2).getLow(), quotes.get(i), quotes.get(i).getHigh()));
                log.debug("ADD Down Box {} ", quotes.get(i).getTradeDate());
            }

            //Bar Count
            if (lookBackType == 0) {
                // left < bar_index-lb
                upBoxQueue.removeIf(fvgBox -> quotes.indexOf(current) - lookBackNumber > quotes.indexOf(fvgBox.getLeftQuote()));
                // left < bar_index-lb
                downBoxQueue.removeIf(fvgBox -> quotes.indexOf(current) - lookBackNumber > quotes.indexOf(fvgBox.getLeftQuote()));
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
//            StringBuilder stringBuilder = new StringBuilder();
//            for (FVGBox box : downBoxQueue) {
//                stringBuilder.append(box.getQuote().getTradeDateStr());
//                stringBuilder.append("|");
//            }
//            log.debug("down queue {}", stringBuilder.toString());

            // strategy 判斷進出場
            FVGResult lastResult = results.isEmpty() ? null : results.get(results.size() - 1);
            FVGPosition lastPosition = lastResult == null ? FVGPosition.EMPTY : lastResult.getPosition();
            FVGPosition position = FVGPosition.EMPTY;
            if (current.getClose() > downValuesAvg && FVGPosition.EMPTY.equals(lastPosition)) {
                //當價格突破熊市平均 買入
                position = FVGPosition.BUY;
            } else if (FVGPosition.BUY.equals(lastPosition) || FVGPosition.HOLD.equals(lastPosition)) {
                if (current.getClose() < upValuesAvg) {
                    //當價格跌破牛市平均 賣出
                    position = FVGPosition.SELL;
                } else {
                    //持有但不到賣出標準
                    position = FVGPosition.HOLD;
                }
            }


            FVGResult result = new FVGResult();
            result.setHighest(hst);
            result.setLowest(lst);
            result.setUpAvgValue(upValuesAvg);
            result.setDownAvgValue(downValuesAvg);
            result.setPosition(position);
            result.getUpBoxList().addAll(upBoxQueue.stream().toList());
            result.getDownBoxList().addAll(downBoxQueue.stream().toList());
            results.add(result);
        }

        log.info("End");

        return results;
    }
}
