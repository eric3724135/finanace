package com.eric.service;

import com.eric.domain.*;
import com.eric.histock.HiStockDataHandler;
import com.eric.parser.ParserResult;
import com.eric.persist.pojo.QuoteDto;
import com.eric.persist.repo.QuoteRepository;
import com.eric.yahoo.YahooUSQuoteParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class QuoteService {

    @Autowired
    private QuoteRepository quoteRepository;

    public Quote addQuote(Quote quote) {
        QuoteDto dto;
        try {
            dto = quoteRepository.save(quote.convertToQuoteDto());
            return dto.getQuoteObj();
        } catch (Exception e) {
            log.error("[QuoteService] addQuote ex ", e);
            return null;
        }

    }

    public Quote getLatestQuote(String id) {
        QuoteDto dto = quoteRepository.findLatestById(id);
        return dto == null ? null : dto.getQuoteObj();
    }

    public List<Quote> getLatestRSIQuotes(LocalDate date, String source) {
        List<QuoteDto> quotes = quoteRepository.findLatestByDate(date, source);
        List<Quote> results = new ArrayList<>();
        quotes.forEach(quoteDto -> {
            results.add(quoteDto.getQuoteObj());
        });
        return results;
    }

    public boolean getQuoteExist(String id, LocalDate date) {
//        ExampleMatcher matcher = ExampleMatcher.matchingAll()
//                .withMatcher("symbol", ExampleMatcher.GenericPropertyMatchers.exact())
//                .withMatcher("tradeDate", ExampleMatcher.GenericPropertyMatchers.exact());
//        QuoteDto dto = new QuoteDto();
//        dto.setSymbol(id);
//        dto.setTradeDate(date);
//        Example<QuoteDto> example = Example.of(dto, matcher);
        List<QuoteDto> result = quoteRepository.findByIdAndTradeDate(id, date);
        return result != null && !result.isEmpty();
    }

    public List<Quote> getTweQuotesFromSite(Symbol symbol) {
        HiStockDataHandler parser = new HiStockDataHandler(symbol, Period.ONE_DAY, 20);
        ParserResult<Quote> quoteResult = parser.getResult();
        if (quoteResult.isSuccess()) {
            List<Quote> quotes = quoteResult.getResultList();
            Collections.sort(quotes, (o1, o2) -> o2.getTradeDate().compareTo(o1.getTradeDate()));
            return quotes;
        } else {
            return new ArrayList<>();
        }

    }

    public List<Quote> getusQuotesFromSite(Symbol symbol) {
        //hiStock 查無 查yahoo quote
        YahooUSQuoteParser yahooUSQuoteParser = new YahooUSQuoteParser(new UsSymbol(symbol.getId(), symbol.getName()), "6mo");
        ParserResult<USQuote> usQuoteResult = yahooUSQuoteParser.getResult();
        List<Quote> usResult = new ArrayList<>();
        usQuoteResult.getResultList().forEach(usQuote -> usResult.add(this.convertUSQuote(usQuote)));
        Collections.sort(usResult, (o1, o2) -> o2.getTradeDate().compareTo(o1.getTradeDate()));
        return usResult;
    }

    private Quote convertUSQuote(USQuote usQuote) {
        Quote quote = Quote.buildSimpleQuote(new Symbol(usQuote.getSymbol(), usQuote.getName()), usQuote.getTradeDate());
        quote.setOpen(usQuote.getOpen());
        quote.setHigh(usQuote.getHigh());
        quote.setLow(usQuote.getLow());
        quote.setClose(usQuote.getClose());
        quote.setVolume(usQuote.getVolume());
        quote.setSource(Quote.US_QUOTE);
        return quote;
    }

}
