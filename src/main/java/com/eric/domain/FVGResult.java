package com.eric.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FVGResult {

    private FVGPosition position;

    /**
     * 5日內最高價
     */
    private double highest;
    /**
     * 5日內最低價
     */
    private double lowest;

    /**
     * 上漲 FVG 底部平均價格
     */
    private double upAvgValue;
    /**
     * 下跌 FVG 頂部平均價格
     */
    private double downAvgValue;

    private List<FVGBox> upBoxList = new ArrayList<>();

    private List<FVGBox> downBoxList = new ArrayList<>();

}
