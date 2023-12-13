package com.eric.service;

import com.eric.domain.Quote;
import com.eric.domain.Symbol;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AnalysisService {

    public List<Quote> handleRSI(Symbol symbol, List<Quote> quotes, int rsiValue) {
        BarSeries series = new BaseBarSeriesBuilder().withName(symbol.getId()).build();
//        for (int i = quotes.size() - 1; i > 0; i--) {
//            CMQuote cmQuote = quotes.get(i);
//            series.addBar(ZonedDateTime.of(cmQuote.getTradeDate(), ZoneId.systemDefault()), cmQuote.getOpen(), cmQuote.getHigh(), cmQuote.getLow(), cmQuote.getClose(), cmQuote.getVolume());
//        }
        Collections.reverse(quotes);
        quotes.forEach(cmQuote -> {
            series.addBar(ZonedDateTime.of(cmQuote.getTradeDate().atStartOfDay(), ZoneId.systemDefault()), cmQuote.getOpen(), cmQuote.getHigh(), cmQuote.getLow(), cmQuote.getClose(), cmQuote.getVolume());
        });
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 6);
        for (int i = quotes.size() - 1; i > quotes.size() - 31; i--) {
            Quote quote = quotes.get(i);
            quote.setRsi5(rsiIndicator.getValue(i).doubleValue());
        }

        Collections.reverse(quotes);
        return quotes;
    }
}
