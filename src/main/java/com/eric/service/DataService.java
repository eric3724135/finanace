package com.eric.service;

import com.eric.csv.TdccStockDistributionCsvParser;
import com.eric.domain.Quote;
import com.eric.persist.pojo.FavoriteSymbolDto;
import com.eric.persist.pojo.QuoteDto;
import com.eric.persist.pojo.TdccStockDistribution;
import com.eric.persist.repo.FavoriteSymbolRepository;
import com.eric.persist.repo.QuoteRepository;
import com.eric.persist.repo.TdccStockDistributionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataService {
    @Autowired
    private TdccStockDistributionRepository stockDistributionRepository;
    @Autowired
    private FavoriteSymbolRepository favoriteSymbolRepository;

    public void syncTdccStockDistribution() {
        TdccStockDistributionCsvParser parser = new TdccStockDistributionCsvParser();
        try {
            List<TdccStockDistribution> distributionList = parser.parserFile();
            Map<String, TdccStockDistribution> distributionMap = distributionList.stream()
                    .collect(Collectors.toMap(
                            TdccStockDistribution::getSymbol,  // Key mapper
                            obj -> obj,         // Value mapper
                            (existing, replacement) -> existing       // Supplier for the resulting Map
                    ));
            List<FavoriteSymbolDto> symbolDtos = favoriteSymbolRepository.findByType("0");
            symbolDtos.forEach(symbolDto -> {
                TdccStockDistribution distribution = distributionMap.get(symbolDto.getId());
                if (distribution != null) {
                    distribution.setName(symbolDto.getName());
                    stockDistributionRepository.deleteById(symbolDto.getId());
                    stockDistributionRepository.save(distribution);
                }
            });

        } catch (Exception e) {
            log.error("取得大戶資料失敗");
        }
    }
}
