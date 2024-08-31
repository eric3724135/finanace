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

    public List<Quote> handleRSI(Symbol symbol, List<Quote> quotes, int rsiValue, boolean limited) {
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
        int limit = 0;
        if (limited) {
            limit = series.getBarCount() < 31 ? 0 : series.getBarCount() - 31;
        }
        for (int i = series.getBarCount() - 1; i > limit; i--) {
            Quote quote = quotes.get(i);
            try {
                if (rsiValue <= 6) {
                    quote.setRsi5(rsiIndicator.getValue(i).doubleValue());
                } else {
                    quote.setRsi10(rsiIndicator.getValue(i).doubleValue());
                }
            } catch (Exception e) {
                log.error("[{}] error", quote.getSymbol(), e);
            }

        }

        Collections.reverse(quotes);
        return quotes;
    }
}
