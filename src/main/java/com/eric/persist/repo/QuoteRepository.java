package com.eric.persist.repo;

import com.eric.persist.pojo.QuoteDto;
import com.eric.persist.pojo.SymbolDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;


public interface QuoteRepository extends JpaRepository<QuoteDto, String> {

    @Query(value = "SELECT * from quote where symbol = ?1 order by trade_date desc Limit 1", nativeQuery = true)
    QuoteDto findLatestById(String id);

    @Query(value = "SELECT * from quote where trade_date = ?1 order by symbol", nativeQuery = true)
    List<QuoteDto> findLatestByDate(LocalDate date);

}
