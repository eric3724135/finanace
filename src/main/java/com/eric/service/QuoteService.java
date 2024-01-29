package com.eric.service;

import com.eric.domain.*;
import com.eric.histock.HiStockDataHandler;
import com.eric.parser.ParserResult;
import com.eric.persist.pojo.QuoteDto;
import com.eric.persist.pojo.SymbolDto;
import com.eric.persist.repo.QuoteRepository;
import com.eric.yahoo.YahooUSQuoteParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.eric.domain.SymbolCounter.*;
import static com.eric.domain.SymbolCounter.usSymbolSize;

@Service
@Slf4j
public class QuoteService {

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private SymbolService symbolService;

    private Future<?> tweFuture;
    private Future<?> usFuture;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Autowired
    private Ta4jIndicatorService indicatorService;

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

    @Scheduled(cron = "0 0 15 * * ?")
    public void scheduleTweDailyQuote() {
        log.info("台股同步批次啟動");

        List<SymbolDto> tweSymbols = symbolService.getSymbolsFromLocal(SymbolType.TWE);
        SymbolCounter.tweSymbolSize = tweSymbols.size();
        SymbolCounter.tweSymbolCnt = 0;
        if (tweFuture != null && !tweFuture.isDone()) {
            return;
        }
        tweFuture = executorService.submit(() -> {
            tweSymbols.forEach(symbol -> {
                tweSymbolCnt++;
                log.debug("[{}] {} Sync", symbol.getId(), symbol.getName());
                Quote latestQuote = this.getLatestQuote(symbol.getId());
                LocalDate today = LocalDate.now();
                if (today.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                    today = today.minus(2, ChronoUnit.DAYS);
                }
                if (today.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                    today = today.minus(1, ChronoUnit.DAYS);
                }

                if (latestQuote == null
                        || latestQuote.getTradeDate().isBefore(today)
                        || latestQuote.getTradeDate().isEqual(today)) {
                    List<Quote> quotes = this.getTweQuotesFromSite(symbol.getSymbolObj());
                    if (quotes == null || quotes.isEmpty()) {
                        return;
                    }
                    Quote quote = quotes.get(0);
                    if (quote != null && quote.getRsi5() < 20) {
                        try {
                            boolean isExist = this.getQuoteExist(quote.getSymbol(), quote.getTradeDate());
                            if (!isExist) {
                                Quote result = this.addQuote(quote);
                                log.info("[{}] {} {} GET", symbol.getId(), symbol.getName(), result.getTradeDate());
                            }
                        } catch (Exception e) {
                            log.error("[{}] exception", symbol.getId(), e);
                        }
                    }
                }

            });

        });

    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void scheduleUSDailyQuote() {
        log.info("美股同步批次啟動");
        List<SymbolDto> usSymbols = symbolService.getSymbolsFromLocal(SymbolType.US);
        usSymbolSize = usSymbols.size();
        usSymbolCnt = 0;
        if (usFuture != null && !usFuture.isDone()) {
           return;
        }
        usFuture = executorService.submit(() -> {

            usSymbols.forEach(usSymbol -> {
                usSymbolCnt++;
                log.debug("[{}] {} Sync", usSymbol.getId(), usSymbol.getName());
                Quote latestQuote = this.getLatestQuote(usSymbol.getId());
                LocalDate today = LocalDate.now();
                if (today.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                    today = today.minus(2, ChronoUnit.DAYS);
                }
                if (today.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                    today = today.minus(1, ChronoUnit.DAYS);
                }

                if (latestQuote == null || latestQuote.getTradeDate().isBefore(today) || latestQuote.getTradeDate().isEqual(today)) {
                    List<Quote> quotes = this.getusQuotesFromSite(usSymbol.getSymbolObj());
                    if (quotes == null || quotes.isEmpty()) {
                        return;
                    }
                    this.indicatorService.fillRsiValue(usSymbol.getId(), quotes, 6);
                    this.indicatorService.fillMa120Value(usSymbol.getId(), quotes);

                    Quote quote = quotes.get(0);
                    if (quote != null && quote.getRsi5() < 20) {
                        try {
                            boolean isExist = this.getQuoteExist(quote.getSymbol(), quote.getTradeDate());
                            if (!isExist) {
                                Quote result = this.addQuote(quote);
                                log.info("[{}] {} {} GET", usSymbol.getId(), usSymbol.getName(), result.getTradeDate());
                            }

                        } catch (Exception e) {
                            log.error("[{}] exception", usSymbol.getId(), e);
                        }
                    }
                }

            });
        });
    }

}
