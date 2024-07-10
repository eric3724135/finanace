package com.eric.persist.repo;

import com.eric.persist.pojo.FVGRecordDto;
import com.eric.persist.pojo.FvgProfit;
import org.springframework.data.jpa.repository.JpaRepository;
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
    List<FVGRecordDto> findTweStillHoldBuy();

    @Query(value = "SELECT a.* FROM FVG_RECORD a INNER JOIN ( SELECT id, MAX(trade_date) trade_date   FROM FVG_RECORD GROUP BY ID) b ON a.id= b.id AND a.trade_date = b.trade_date WHERE a.position ='BUY' and a.id > '999999' order by trade_date desc", nativeQuery = true)
    List<FVGRecordDto> findUsStillHoldBuy();

    @Query(value = "SELECT \n" +
            "    t1.id AS id,\n" +
            "\tt1.name AS name,\n" +
            "    t1.trade_date AS buyDate,\n" +
            "    t1.close AS buyPrice,\n" +
            "    t2.trade_date AS sellDate,\n" +
            "    t2.close AS sellPrice,\n" +
            "\tt2.close - t1.close AS profit\n" +
            "FROM \n" +
            "    FVG_RECORD  t1\n" +
            "LEFT JOIN FVG_RECORD  t2 \n" +
            "    ON t2.position = 'SELL' AND \n" +
            "\tt1.id = t2.id and\n" +
            "\tt2.trade_date = (\n" +
            "        SELECT MIN(t3.trade_date)\n" +
            "        FROM FVG_RECORD  t3\n" +
            "        WHERE t3.position = 'SELL' AND t3.trade_date > t1.trade_date\n" +
            "    )\n" +
            "WHERE \n" +
            "    t1.position = 'BUY'\n" +
            "and t1.id < '9999'\n" +
            "and t2.close is not null\n" +
            "order by  t2.trade_date desc;", nativeQuery = true)
    List<FvgProfit> getTWEProfitReport();

    @Query(value = "SELECT \n" +
            "    t1.id AS id,\n" +
            "\tt1.name AS name,\n" +
            "    t1.trade_date AS buyDate,\n" +
            "    t1.close AS buyPrice,\n" +
            "    t2.trade_date AS sellDate,\n" +
            "    t2.close AS sellPrice,\n" +
            "\tt2.close - t1.close AS profit\n" +
            "FROM \n" +
            "    FVG_RECORD  t1\n" +
            "LEFT JOIN FVG_RECORD  t2 \n" +
            "    ON t2.position = 'SELL' AND \n" +
            "\tt1.id = t2.id and\n" +
            "\tt2.trade_date = (\n" +
            "        SELECT MIN(t3.trade_date)\n" +
            "        FROM FVG_RECORD  t3\n" +
            "        WHERE t3.position = 'SELL' AND t3.trade_date > t1.trade_date\n" +
            "    )\n" +
            "WHERE \n" +
            "    t1.position = 'BUY'\n" +
            "and t1.id > '9999'\n" +
            "and t2.close is not null\n" +
            "order by  t2.trade_date desc;", nativeQuery = true)
    List<FvgProfit> getUSProfitReport();
}
