package com.eric.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FVGBox {

    private Quote quote;

    private Quote leftQuote;

    private double low;

    private Quote rightQuote;

    private double high;

    public FVGBox(Quote quote, Quote leftQuote, double low, Quote rightQuote, double high) {
        this.quote = quote;
        this.leftQuote = leftQuote;
        this.low = low;
        this.rightQuote = rightQuote;
        this.high = high;
    }
}
