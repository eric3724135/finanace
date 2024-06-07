package com.eric.persist.repo;

import com.eric.persist.pojo.ProfitDto;
import com.eric.persist.pojo.SymbolDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;


public interface ProfitRepository extends JpaRepository<ProfitDto, String> {

    @Query(value = "SELECT * from profit  where id = ?1 and strategy = ?2 order by update_date desc", nativeQuery = true)
    List<ProfitDto> findBySymbol(String symbol,String strategy);


    @Query(value = "SELECT * from profit  where id = ?1 and strategy = ?2 and update_date between ?3 and ?4 order by update_date desc", nativeQuery = true)
    List<ProfitDto> findBySymbolAndUpdateDate(String symbol, String strategy, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT * from profit  where id = ?1 and strategy = ?2 and buy_date = ?3 ", nativeQuery = true)
    ProfitDto findBySymbolAndBuyDate(String symbol, String strategy, LocalDate buyDate);
}
