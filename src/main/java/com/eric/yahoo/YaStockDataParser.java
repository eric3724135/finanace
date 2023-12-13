package com.eric.yahoo;

import com.eric.domain.Quote;
import com.eric.domain.Period;
import com.eric.domain.Symbol;
import com.eric.parser.ParserError;
import com.eric.parser.ParserResult;
import com.eric.utils.JsonUtils;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
public class YaStockDataParser extends YahooAbstractParser<Quote> {
    public YaStockDataParser(Symbol symbol, Period period) {
        super(symbol, period);
    }

    @Override
    public ParserResult<Quote> getResult() {
        ParserResult<Quote> result = new ParserResult<>();
        String jsonStr = "";
        try {
            //金鑰跟header "Refer"ˊ值有相關
            jsonStr = this.getResponse().body();
            ObjectMapper mapper = JsonUtils.getMapper();
            mapper.configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS.mappedFeature(), true);
            jsonStr = jsonStr.substring(5, jsonStr.length() - 2);
            JsonNode dataNode = mapper.readValue(jsonStr, JsonNode.class);
            JsonNode arrNode = dataNode.get("ta");
            if (arrNode == null || arrNode.isEmpty()) {
                result.setSuccess(false);
                result.setError(ParserError.JSOUP_SYMBOL_ERROR);
                return result;
            }
            if (arrNode.isArray()) {
                for (JsonNode node : arrNode) {
                    Quote data = Quote.ofFromYahoo(this.getSymbol(), this.getPeriod(), node);
                    if (data != null) {
                        result.getResultList().add(data);
                    }
                }
            }

            Set<Quote> set = new HashSet<>(result.getResultList());
            List<Quote> list = new ArrayList<>(set);
            list.sort(Comparator.comparing(Quote::getTradeDate));
            result.setResultList(list);
//            this.handleKD(list);

            result.setSuccess(true);
//            log.debug("[{}] 解析成功 ", getSymbol());
            return result;
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(ParserError.JSOUP_PARSE_ERROR);
            log.error(jsonStr == null ? "" : jsonStr, e);
            log.error("[{}] {}", getSymbol().getId(), getSymbol().getName(), e);

        }
        return result;
    }


    private void handleKD(List<Quote> quotes) {
        DecimalFormat df = new DecimalFormat("##.00");
        for (int i = 8; i < quotes.size(); i++) {
            Quote quote = quotes.get(i);
            if (i == 8) {
                quote.setK9(50);
                quote.setD9(50);
            } else {
                BarSeries series = new BaseBarSeriesBuilder().withName(getSymbol().getId()).build();
                for (int j = 9; j >= 0; j--) {
                    Quote refQuote = quotes.get(i - j);
                    series.addBar(ZonedDateTime.of(refQuote.getTradeDate().atStartOfDay(), ZoneId.systemDefault()), refQuote.getOpen(), refQuote.getHigh(), refQuote.getLow(), refQuote.getClose(), refQuote.getVolume());
                }
                StochasticOscillatorKIndicator kIndicator = new StochasticOscillatorKIndicator(series, 10);
                double rsv = kIndicator.getValue(9).doubleValue();
                double pastK = quotes.get(i - 1).getK9();
                double pastD = quotes.get(i - 1).getD9();
                double k = (pastK * 2 / 3) + (rsv / 3);
                double d = (pastD * 2 / 3) + (k / 3);

                try {
                    quote.setK9(Double.parseDouble(df.format(k)));
                    quote.setD9(Double.parseDouble(df.format(d)));
                } catch (Exception e) {
                    log.error("[{}] {} {} K {} D {}", quote.getSymbol(), quote.getName(), quote.getTradeDate(), quote.getK9(), quote.getD9());
                }
            }
        }
    }
}
