package com.eric.service;

import com.eric.domain.Quote;
import com.eric.domain.Symbol;
import com.eric.persist.pojo.FavoriteSymbolDto;
import com.eric.persist.pojo.QuoteDto;
import com.eric.persist.repo.FavoriteSymbolRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RsiCalService {


    @Autowired
    private FavoriteSymbolRepository favoriteSymbolRepository;

    @Autowired
    private QuoteService quoteService;
    @Autowired
    private AnalysisService analysisService;

    public List<Quote> analysisSymbolRsiAvg(String symbol) {
        Optional<FavoriteSymbolDto> optionalSymbol = favoriteSymbolRepository.findById(symbol);
        List<Quote> rsiQuotes = new ArrayList<>();
        if (optionalSymbol.isPresent()) {
            FavoriteSymbolDto symbolDto = optionalSymbol.get();
            List<Quote> oriQuotes = null;
            if ("0".equals(symbolDto.getType())) {
                oriQuotes = quoteService.getusQuotesFromSite(Symbol.ofTW(symbolDto.getId(), symbolDto.getName()), "1d", "10y");
                if (oriQuotes == null || oriQuotes.isEmpty()) {
                    oriQuotes = quoteService.getusQuotesFromSite(Symbol.ofTWO(symbolDto.getId(), symbolDto.getName()), "1d", "10y");
                }
            } else {
                oriQuotes = quoteService.getusQuotesFromSite(symbolDto.getSymbolObj(), "1d", "10y");
            }
            rsiQuotes = analysisService.handleRSI(symbolDto.getSymbolObj(), oriQuotes, 6, false);
        }

        rsiQuotes = rsiQuotes.subList(0, rsiQuotes.size() < 2000 ? rsiQuotes.size() : 2000);

        List<Quote> filteredQuotes = new ArrayList<>();

        for (Quote current : rsiQuotes) {
            boolean shouldAdd = true;

            for (int i = 0; i < filteredQuotes.size(); i++) {
                Quote existing = filteredQuotes.get(i);
                long daysBetween = ChronoUnit.DAYS.between(existing.getTradeDate(), current.getTradeDate());

                if (Math.abs(daysBetween) <= 20) {
                    if (current.getRsi5() < existing.getRsi5()) {
                        filteredQuotes.set(i, current); // Replace with lower RSI Quote
                    }
                    shouldAdd = false; // Do not add the current Quote since we have handled it
                    break;
                }
            }

            if (shouldAdd) {
                filteredQuotes.add(current);
            }
        }

        List<Quote> result = filteredQuotes.stream()
                .sorted(Comparator.comparingDouble(Quote::getRsi5))
                .collect(Collectors.toList());

        return result;

    }
}
