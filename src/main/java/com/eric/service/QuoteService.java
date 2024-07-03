package com.eric.service;

import com.eric.domain.*;
import com.eric.excel.USStockExcelReportHandler;
import com.eric.histock.HiStockDataHandler;
import com.eric.mail.MailConfig;
import com.eric.mail.MailUtils;
import com.eric.parser.ParserResult;
import com.eric.persist.pojo.FavoriteSymbolDto;
import com.eric.persist.pojo.QuoteDto;
import com.eric.persist.pojo.SymbolDto;
import com.eric.persist.repo.FavoriteSymbolRepository;
import com.eric.persist.repo.QuoteRepository;
import com.eric.wessiorfinance.util.TLPosition;
import com.eric.yahoo.YahooUSQuoteParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private FavoriteSymbolRepository favoriteSymbolRepository;

    @Autowired
    private SymbolService symbolService;
    @Autowired
    private WessiorFintechService wessiorService;

    private Future<?> tweFuture;
    private Future<?> usFuture;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Autowired
    private Ta4jIndicatorService indicatorService;
    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private MailConfig mailConfig;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

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
            Quote quote = quoteDto.getQuoteObj();
            Optional<FavoriteSymbolDto> optionalSymbol = favoriteSymbolRepository.findById(quoteDto.getSymbol());
            if (optionalSymbol.isPresent()) {
                FavoriteSymbolDto symbolDto = optionalSymbol.get();
                quote.setCategory(symbolDto.getCategory());
            }
            results.add(quote);
        });
        results.sort(Comparator.comparingDouble(Quote::getRsi5));
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
            quotes.sort((o1, o2) -> o2.getTradeDate().compareTo(o1.getTradeDate()));
            return quotes;
        } else {
            return new ArrayList<>();
        }

    }

    public List<Quote> getusQuotesFromSite(Symbol symbol, String interval, String range) {
        //hiStock 查無 查yahoo quote
        YahooUSQuoteParser yahooUSQuoteParser = new YahooUSQuoteParser(new UsSymbol(symbol.getId(), symbol.getName()), range, interval);
        ParserResult<USQuote> usQuoteResult = yahooUSQuoteParser.getResult();
        List<Quote> usResult = new ArrayList<>();
        usQuoteResult.getResultList().forEach(usQuote -> {
            if (usQuote.getClose() > 0) {
                usResult.add(this.convertUSQuote(usQuote));
            }
        });
        usResult.sort((o1, o2) -> o2.getTradeDate().compareTo(o1.getTradeDate()));
        //發現week quote 可能出現trade date 重複(時間不同)
        if (usResult.size() > 2 &&
                usResult.get(0).getTradeDate().isEqual(usResult.get(1).getTradeDate())) {
            usResult.remove(0);
        }
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

    @Scheduled(cron = "0 0 17 * * ?")
    public void scheduleTweDailyQuote() {
        log.info("台股同步批次啟動");

        List<FavoriteSymbolDto> tweSymbols = symbolService.getFavoriteSymbols(SymbolType.TWE);
//        List<SymbolDto> tweSymbols = symbolService.getSymbolsFromLocal(SymbolType.TWE);
        SymbolCounter.tweSymbolSize = tweSymbols.size();
        SymbolCounter.tweSymbolCnt = 0;
        if (tweFuture != null && !tweFuture.isDone()) {
            return;
        }
        tweFuture = executorService.submit(() -> {
            List<Quote> excelQuotes = new ArrayList<>();
            tweSymbols.forEach(symbol -> {
                tweSymbolCnt++;
                log.debug("[{}] {} Sync", symbol.getId(), symbol.getName());
                Quote latestQuote = this.getLatestQuote(symbol.getId());
                LocalDate today = LocalDate.now();
                if (today.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                    today = today.minusDays(2);
                }
                if (today.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                    today = today.minusDays(1);
                }

                if (latestQuote == null
                        || latestQuote.getTradeDate().isBefore(today)
                        || latestQuote.getTradeDate().isEqual(today)) {
                    List<Quote> quotes = this.getTweQuotesFromSite(symbol.getSymbolObj());
                    if (quotes == null || quotes.isEmpty()) {
                        return;
                    }
                    Quote quote = quotes.get(0);

                    if (quote != null && (quote.getRsi5() < 20 || quote.getRsi5() > 93)) {
                        if (quote.getVolume() * quote.getClose() < 50000) {
                            log.info("[{}] {} {} 成交值過小不採納", symbol.getId(), symbol.getName(), quote.getVolume() * quote.getClose());
                            return;
                        }
                        try {
                            boolean isExist = this.getQuoteExist(quote.getSymbol(), quote.getTradeDate());
                            if (!isExist) {
                                // rsi 24 week rsi6
                                this.analysisService.handleRSI(symbol.getSymbolObj(), quotes, 24);
                                List<Quote> weekQuotes = this.getusQuotesFromSite(Symbol.ofTW(symbol.getId(), symbol.getName()), "1wk", "1y");
                                if (weekQuotes == null || weekQuotes.isEmpty()) {
                                    weekQuotes = this.getusQuotesFromSite(Symbol.ofTWO(symbol.getId(), symbol.getName()), "1wk", "1y");
                                }
                                if (weekQuotes != null && weekQuotes.size() > 30) {
                                    this.analysisService.handleRSI(symbol.getSymbolObj(), weekQuotes, 6);
                                    //不調整欄位借用kd diff來存週
                                    Quote latestWeekQuote = weekQuotes.get(0);
                                    quote.setKdDiff(latestWeekQuote.getRsi5());
                                }
                                TLPosition position = null;
                                try {
                                    position = wessiorService.getTLStatus(quote.getSymbolObj(), today);

                                } catch (Exception e) {
                                    log.error("[{}] {} getTLStatus error", quote.getSymbol(), quote.getName(), e);
                                    //TODO fix plan
                                }
                                Quote result = this.addQuote(quote);
                                result.setTlPosition(position);
                                excelQuotes.add(result);
                                log.info("[{}] {} {} GET", symbol.getId(), symbol.getName(), result.getTradeDate());
                            }
                        } catch (Exception e) {
                            log.error("[{}] exception", symbol.getId(), e);
                        }
                    }
                }

            });
            if (!excelQuotes.isEmpty()) {
                excelQuotes.sort(Comparator.comparing(Quote::getRsi5));
                for(Quote quote: excelQuotes){
                    Optional<FavoriteSymbolDto> optionalSymbol = favoriteSymbolRepository.findById(quote.getSymbol());
                    if (optionalSymbol.isPresent()) {
                        FavoriteSymbolDto symbolDto = optionalSymbol.get();
                        quote.setCategory(symbolDto.getCategory());
                    }
                }
                USStockExcelReportHandler handler = new USStockExcelReportHandler();
                try {
                    ByteArrayOutputStream bos = handler.export(excelQuotes);
                    MailUtils.generateAndSendEmail(mailConfig, mailConfig.getAddressArr(),
                            String.format("%s_台股每日挑檔", LocalDate.now().format(dateFormatter)),
                            String.format("%s_台股每日挑檔", LocalDate.now().format(dateFormatter)),
                            String.format("%s_台股.xlsx", LocalDate.now().format(dateFormatter)), bos);
                } catch (IOException | MessagingException e) {
                    log.error("[USStockExcelReportHandler] error ", e);
                }
            }

        });

    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void scheduleUSDailyQuote() {
        log.info("美股同步批次啟動");
        List<FavoriteSymbolDto> usSymbols = symbolService.getFavoriteSymbols(SymbolType.US);
        usSymbolSize = usSymbols.size();
        usSymbolCnt = 0;
        if (usFuture != null && !usFuture.isDone()) {
            return;
        }
        usFuture = executorService.submit(() -> {
            List<Quote> excelQuotes = new ArrayList<>();
            usSymbols.forEach(usSymbol -> {
                usSymbolCnt++;
                log.debug("[{}] {} Sync", usSymbol.getId(), usSymbol.getName());
                Quote latestQuote = this.getLatestQuote(usSymbol.getId());
                LocalDate today = LocalDate.now();
                if (today.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                    today = today.minusDays(2);
                }
                if (today.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                    today = today.minusDays(1);
                }

                if (latestQuote == null || latestQuote.getTradeDate().isBefore(today) || latestQuote.getTradeDate().isEqual(today)) {
                    List<Quote> quotes = this.getusQuotesFromSite(usSymbol.getSymbolObj(), "1d", "6mo");
                    if (quotes == null || quotes.isEmpty()) {
                        return;
                    }

                    this.analysisService.handleRSI(usSymbol.getSymbolObj(), quotes, 6);
                    this.analysisService.handleRSI(usSymbol.getSymbolObj(), quotes, 24);

                    this.indicatorService.fillMa120Value(usSymbol.getId(), quotes);

                    Quote quote = quotes.get(0);
                    if (quote != null && (quote.getRsi5() < 20 || quote.getRsi5() > 93)) {
                        try {
                            boolean isExist = this.getQuoteExist(quote.getSymbol(), quote.getTradeDate());
                            if (!isExist) {
                                List<Quote> weekQuotes = this.getusQuotesFromSite(usSymbol.getSymbolObj(), "1wk", "1y");
                                this.analysisService.handleRSI(usSymbol.getSymbolObj(), weekQuotes, 6);
                                TLPosition position = null;
                                try {
                                    position = wessiorService.getTLStatus(quote.getSymbolObj(), today);

                                } catch (Exception e) {
                                    log.error("[{}] {} getTLStatus error", quote.getSymbol(), quote.getName(), e);
                                }
                                //不調整欄位借用kd diff來存週
                                Quote latestWeekQuote = weekQuotes.get(0);
                                quote.setKdDiff(latestWeekQuote.getRsi5());
                                Quote result = this.addQuote(quote);
                                result.setTlPosition(position);
                                excelQuotes.add(result);
                                log.info("[{}] {} {} GET", usSymbol.getId(), usSymbol.getName(), result.getTradeDate());
                            }

                        } catch (Exception e) {
                            log.error("[{}] exception", usSymbol.getId(), e);
                        }

                    }
                }

            });
            if (!excelQuotes.isEmpty()) {
                excelQuotes.sort(Comparator.comparing(Quote::getRsi5));
                for(Quote quote: excelQuotes){
                    Optional<FavoriteSymbolDto> optionalSymbol = favoriteSymbolRepository.findById(quote.getSymbol());
                    if (optionalSymbol.isPresent()) {
                        FavoriteSymbolDto symbolDto = optionalSymbol.get();
                        quote.setCategory(symbolDto.getCategory());
                    }
                }
                USStockExcelReportHandler handler = new USStockExcelReportHandler();
                try {
                    ByteArrayOutputStream bos = handler.export(excelQuotes);
                    MailUtils.generateAndSendEmail(mailConfig, mailConfig.getAddressArr(),
                            String.format("%s_美股每日挑檔", LocalDate.now().format(dateFormatter)),
                            String.format("%s_美股每日挑檔", LocalDate.now().format(dateFormatter)),
                            String.format("%s_美股.xlsx", LocalDate.now().format(dateFormatter)), bos);
                } catch (IOException | MessagingException e) {
                    log.error("[USStockExcelReportHandler] error ", e);
                }
            }
        });
    }

}
