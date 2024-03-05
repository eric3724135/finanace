package com.eric.service;

import com.eric.domain.Quote;
import com.eric.domain.Symbol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class AnalysisService {

    public List<Quote> handleRSI(Symbol symbol, List<Quote> quotes, int rsiValue) {
        BarSeries series = new BaseBarSeriesBuilder().withName(symbol.getId()).build();
        Collections.reverse(quotes);
        quotes.forEach(cmQuote -> {
            if (cmQuote.getClose() == 0) {
                return;
            }
            try {
                series.addBar(ZonedDateTime.of(cmQuote.getTradeDate().atStartOfDay(), ZoneId.systemDefault()), cmQuote.getOpen(), cmQuote.getHigh(), cmQuote.getLow(), cmQuote.getClose(), cmQuote.getVolume());
            } catch (Exception e) {
                log.warn("[{}] error ", cmQuote.getSymbol(), e);

            }
        });
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, rsiValue);
        int limit = quotes.size() < 31 ? 0 : quotes.size() - 31;
        for (int i = quotes.size() - 1; i > limit; i--) {
            Quote quote = quotes.get(i);
            if (rsiValue <= 6) {
                quote.setRsi5(rsiIndicator.getValue(i).doubleValue());
            } else {
                quote.setRsi10(rsiIndicator.getValue(i).doubleValue());
            }

        }

        Collections.reverse(quotes);
        return quotes;
    }
}
