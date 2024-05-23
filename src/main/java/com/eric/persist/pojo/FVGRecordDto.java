package com.eric.persist.pojo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
@Data
@Entity
@Table(name = "fvg_record")
public class FVGRecordDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private long seq;

    @Column(name = "id")
    String id;

    @Column(name = "name")
    String name;
    /**
     * tradeDate
     */
    @Column(name = "trade_date")
    private LocalDate tradeDate;

    /**
     * close
     */
    @Column(name = "close")
    private double close;

    /**
     * upAvg
     */
    @Column(name = "up_avg")
    private double upAvg;

    /**
     * upAvg
     */
    @Column(name = "down_avg")
    private double downAvg;

    public FVGRecordDto() {
    }

}
