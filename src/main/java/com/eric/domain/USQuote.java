package com.eric.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Data
public class USQuote {

    private long seq;
    /**
     * symbol
     */
    private String symbol;
    /**
     * symbol_name
     */
    private String name;
    /**
     * tradeDate
     */
    private LocalDate tradeDate;
    /**
     * open
     */
    private double open;
    /**
     * high
     */
    private double high;
    /**
     * low
     */
    private double low;
    /**
     * close
     */
    private double close;

    /**
     * volume
     */
    private double volume;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        USQuote usQuote = (USQuote) o;
        return seq == usQuote.seq &&
                Double.compare(usQuote.open, open) == 0 &&
                Double.compare(usQuote.high, high) == 0 &&
                Double.compare(usQuote.low, low) == 0 &&
                Double.compare(usQuote.close, close) == 0 &&
                Double.compare(usQuote.volume, volume) == 0 &&
                symbol.equals(usQuote.symbol) &&
                name.equals(usQuote.name) &&
                tradeDate.equals(usQuote.tradeDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, name, tradeDate);
    }
}
