package com.eric.persist.pojo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
@Slf4j
@Data
@Entity
@Table(name = "profit")
public class ProfitDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private long seq;

    @Column(name = "id")
    String id;

    @Column(name = "name")
    String name;

    @Column(name = "strategy")
    private String strategy;

    /**
     * buy_date
     */
    @Column(name = "buy_date")
    private LocalDate buyDate;

    /**
     * buy_price
     */
    @Column(name = "buy_price")
    private double buyPrice;


    /**
     * sell_date
     */
    @Column(name = "sell_date")
    private LocalDate sellDate;

    /**
     * sell_price
     */
    @Column(name = "sell_price")
    private double sellPrice;

    @Column(name = "profit")
    private double profit;

    /**
     * update_date
     */
    @Column(name = "update_date")
    private LocalDate updateDate;

    public ProfitDto(String id, String name, String strategy, LocalDate updateDate) {
        this.id = id;
        this.name = name;
        this.strategy = strategy;
        this.updateDate = updateDate;
    }

    public ProfitDto() {
    }
}
