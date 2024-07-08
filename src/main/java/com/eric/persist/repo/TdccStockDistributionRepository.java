package com.eric.persist.repo;

import com.eric.persist.pojo.FavoriteSymbolDto;
import com.eric.persist.pojo.TdccStockDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface TdccStockDistributionRepository extends JpaRepository<TdccStockDistribution, String> {

    @Query(value = "SELECT * from stock_distribution  where symbol = ?1", nativeQuery = true)
    TdccStockDistribution findBySymbol(String symbol);

}
