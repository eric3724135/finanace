package com.eric.persist.repo;

import com.eric.persist.pojo.FVGRecordDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;


public interface FVGRecordRepository extends JpaRepository<FVGRecordDto, String> {

    @Query(value = "SELECT * from fvg_record where id = ?1 and trade_date = ?2 ", nativeQuery = true)
    List<FVGRecordDto> findByIdAndTradeDate(String id, LocalDate date);
    @Query(value = "SELECT * from fvg_record where position = ?1 and trade_date between ?2 and ?3 order by trade_date desc", nativeQuery = true)
    List<FVGRecordDto> findRecordByPositionAndRange(String position, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT * from fvg_record where trade_date between ?1 and ?2 order by trade_date desc", nativeQuery = true)
    List<FVGRecordDto> findRecordByRange( LocalDate startDate, LocalDate endDate);


}
