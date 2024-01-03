package com.eric.service;

import com.eric.domain.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.RSIIndicator;
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

    public RSIIndicator getRSIIndicator(BarSeries barSeries, int barCount) {
        return new RSIIndicator(new ClosePriceIndicator(barSeries), barCount);
    }

    public void fillRsiValue(String symbol, List<Quote> quotes, int barCount) {
        RSIIndicator rsiIndicator = getRSIIndicator(transfer(symbol, quotes), barCount);
        for (int i = 0; i < quotes.size() / 2; i++) {
            Num value = rsiIndicator.getValue(quotes.size() - i - 1);
            quotes.get(i).setRsi5(value.doubleValue());
        }
    }

    public SMAIndicator getSMAIndicator(String symbol, List<Quote> quotes, int count) {
        return new SMAIndicator(new ClosePriceIndicator(transfer(symbol, quotes)), count);
    }

    public void fillMa120Value(String symbol, List<Quote> quotes) {
        SMAIndicator indicator = getSMAIndicator(symbol, quotes, 120);
        for (int i = 0; i < quotes.size() / 2; i++) {
            Num value = indicator.getValue(quotes.size() - i - 1);
            quotes.get(i).setMa120(value.doubleValue());
        }

    }

}
