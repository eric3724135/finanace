package com.eric.strategy;

import com.eric.domain.Quote;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WWayStrategy {

    public boolean analysis(List<Quote> quotes) {
        if (quotes == null || quotes.size() < 60) {
            return false;
        }
        //1.找出第一個低點
        Quote l1Quote = getLowestQuoteInRange(0, 20, quotes);
        if (l1Quote == null) {
            return false;
        }
        //2.找出最近的高點
        int indexOfL1Quote = quotes.indexOf(l1Quote);
        int h1Range = quotes.size() < indexOfL1Quote + 20 ? 0 : indexOfL1Quote + 20;
        if (h1Range == 0) {
            return false;
        }
        Quote h1Quote = getHighestQuoteInRange(indexOfL1Quote, h1Range, quotes);
        if (h1Quote == null) {
            return false;
        }
        //3.找出第二個低點
        int indexOfH1Quote = quotes.indexOf(h1Quote);
        int l2Range = quotes.size() < indexOfH1Quote + 20 ? 0 : indexOfH1Quote + 20;
        if (l2Range == 0) {
            return false;
        }
        Quote l2Quote = getLowestQuoteInRange(indexOfH1Quote, l2Range, quotes);
        if (l2Quote == null) {
            return false;
        }
        // 3. 找出L2+60日是從高點回落
        int indexOfL2Quote = quotes.indexOf(l2Quote);
        int h2Range = Math.min(quotes.size(), indexOfL2Quote + 60);
        Quote h2Quote = getHighestQuoteInRange(indexOfL2Quote, h2Range, quotes);
        if (h2Quote == null) {
            return false;
        }
        Quote currentQuote = quotes.get(0);
        if (currentQuote.getClose() > h1Quote.getClose() &&
                l1Quote.getClose() > l2Quote.getClose() &&
                h2Quote.getClose()>currentQuote.getClose()) {
            return true;
        }
        return false;

    }

    public Quote getLowestQuoteInRange(int start, int range, List<Quote> baseQuotes) {
        if (baseQuotes == null || baseQuotes.size() < range) {
            return null;
        }
        List<Quote> quotes = baseQuotes.subList(start, range);
        Quote lowestQuote = null;
        for (Quote quote : quotes) {
            if (lowestQuote == null) {
                lowestQuote = quote;
            }
            if (quote.getClose() < lowestQuote.getClose()) {
                lowestQuote = quote;
            }
        }
        return lowestQuote;
    }

    public Quote getHighestQuoteInRange(int start, int range, List<Quote> baseQuotes) {
        if (baseQuotes == null || baseQuotes.size() < range) {
            return null;
        }
        List<Quote> quotes = baseQuotes.subList(start, range);
        Quote highestQuote = null;
        for (Quote quote : quotes) {
            if (highestQuote == null) {
                highestQuote = quote;
            }
            if (quote.getClose() > highestQuote.getClose()) {
                highestQuote = quote;
            }
        }
        return highestQuote;
    }

}
