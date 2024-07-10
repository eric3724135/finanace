package com.eric.persist.pojo;


import java.time.LocalDate;

//@Data
public interface FvgProfit {

    String getId();
    String getName();
    LocalDate getBuyDate();
    double getBuyPrice();
    LocalDate getSellDate();
    double getSellPrice();
    double getProfit();


}
