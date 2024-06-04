package com.eric.persist.repo;

import com.eric.persist.pojo.FVGRecordDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;


public interface FVGRecordRepository extends JpaRepository<FVGRecordDto, String> {

    @Query(value = "SELECT * from fvg_record where id = ?1 and trade_date = ?2 ", nativeQuery = true)
    List<FVGRecordDto> findByIdAndTradeDate(String id, LocalDate date);

    @Query(value = "SELECT * from fvg_record where position = ?1 and trade_date between ?2 and ?3 order by trade_date desc", nativeQuery = true)
    List<FVGRecordDto> findRecordByPositionAndRange(String position, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT * from fvg_record where id < '9999' and trade_date between ?1 and ?2 order by trade_date desc", nativeQuery = true)
    List<FVGRecordDto> findTweRecordByRange(LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT * from fvg_record where id > '9999' and trade_date between ?1 and ?2 order by trade_date desc", nativeQuery = true)
    List<FVGRecordDto> findUsRecordByRange(LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT a.* FROM FVG_RECORD a INNER JOIN ( SELECT id, MAX(trade_date) trade_date   FROM FVG_RECORD GROUP BY ID) b ON a.id= b.id AND a.trade_date = b.trade_date WHERE a.position ='BUY' and a.id < '9999' order by trade_date desc", nativeQuery = true)
    List<FVGRecordDto> findStillHoldBuy();
}
