package com.eric.service;

import com.eric.domain.*;
import com.eric.histock.HiStockDataHandler;
import com.eric.parser.ParserResult;
import com.eric.yahoo.YahooUSQuoteParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class QuoteService {

    public List<CMQuote> getQuotes(Symbol symbol) {
        HiStockDataHandler parser = new HiStockDataHandler(symbol, Period.ONE_DAY, 80);
        ParserResult<CMQuote> quoteResult = parser.getResult();
        if (quoteResult.isSuccess()) {
            List<CMQuote> quotes = quoteResult.getResultList();
            Collections.sort(quotes, (o1, o2) -> o2.getTradeDate().compareTo(o1.getTradeDate()));
            return quotes;
        }
        //hiStock 查無 查yahoo quote
        YahooUSQuoteParser yahooUSQuoteParser = new YahooUSQuoteParser(new UsSymbol(symbol.getId(), symbol.getName()), "6mo");
        ParserResult<USQuote> usQuoteResult = yahooUSQuoteParser.getResult();
        List<CMQuote> usResult = new ArrayList<>();
        usQuoteResult.getResultList().forEach(usQuote -> usResult.add(this.convert(usQuote)));
        Collections.sort(usResult, (o1, o2) -> o2.getTradeDate().compareTo(o1.getTradeDate()));
        return usResult;

    }

    private CMQuote convert(USQuote usQuote) {
        CMQuote quote = CMQuote.buildSimpleQuote(new Symbol(usQuote.getSymbol(), usQuote.getName()), usQuote.getTradeDate());
        quote.setOpen(usQuote.getOpen());
        quote.setHigh(usQuote.getHigh());
        quote.setLow(usQuote.getLow());
        quote.setClose(usQuote.getClose());
        quote.setVolume(usQuote.getVolume());
        return quote;
    }

}
