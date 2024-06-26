package com.eric.service;

import com.eric.domain.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
public class Ta4jIndicatorService {

    public BarSeries transfer(String symbol, List<Quote> quotes) {
        BarSeries barSeries = new BaseBarSeriesBuilder().withName(symbol).build();
        for (int i = quotes.size(); i > 0; i--) {
            Quote quote = quotes.get(i - 1);
            barSeries.addBar(quote.getTradeDate().atStartOfDay().atZone(ZoneId.systemDefault()), quote.getOpen(), quote.getHigh(), quote.getLow(), quote.getClose(), quote.getVolume());
        }

        return barSeries;
    }

    /**
     * sma5Indicator.getValue(sma5Indicator.getBarSeries().getEndIndex()).doubleValue();
     *
     * @param symbol symbol 
     * @param quotes quotes
     * @param count N
     * @return ma(N)
     */
    public SMAIndicator getSMAIndicator(String symbol, List<Quote> quotes, int count) {
        //double ma5 = sma5Indicator.getValue(sma5Indicator.getBarSeries().getEndIndex()).doubleValue();
        return new SMAIndicator(new ClosePriceIndicator(transfer(symbol, quotes)), count);
    }

    public void fillMa120Value(String symbol, List<Quote> quotes) {
        SMAIndicator indicator = getSMAIndicator(symbol, quotes, 120);
        for (int i = 0; i < quotes.size() / 2; i++) {
            Num value = indicator.getValue(quotes.size() - i - 1);
            quotes.get(i).setMa120(value.doubleValue());
        }

    }

    public ATRIndicator getATRIndicator(String symbol, List<Quote> quotes, int count) {
        return new ATRIndicator(this.transfer(symbol, quotes), count);
    }

}
