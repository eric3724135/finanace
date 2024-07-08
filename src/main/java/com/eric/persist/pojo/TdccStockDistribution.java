package com.eric.persist.pojo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Data
@Entity
@Table(name = "stock_distribution")
public class TdccStockDistribution {

    /**
     * symbol
     */
    @Id
    @Column(name = "symbol")
    private String symbol;
    /**
     * symbol_name
     */
    @Column(name = "name")
    private String name;
    /**
     * tradeDate
     */
    @Column(name = "date")
    private LocalDateTime date;
    /**
     * 100張以下比率
     */
    @Column(name = "s100down")
    private double s100down;
    /**
     * 200張以下比率
     */
    @Column(name = "s200down")
    private double s200down;
    /**
     * 400張以下比率
     */
    @Column(name = "s400down")
    private double s400down;
    /**
     * 400張以上比率
     */
    @Column(name = "s400up")
    private double s400up;
    /**
     * 600張以上比率
     */
    @Column(name = "s600up")
    private double s600up;
    /**
     * 800張以上比率
     */
    @Column(name = "s800up")
    private double s800up;
    /**
     * 1000張以上比率
     */
    @Column(name = "s1000up")
    private double s1000up;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TdccStockDistribution that = (TdccStockDistribution) o;
        return Objects.equals(symbol, that.symbol) &&
                Objects.equals(name, that.name) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, name, date);
    }
}
