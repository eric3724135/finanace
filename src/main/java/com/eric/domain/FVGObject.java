package com.eric.domain;

import com.eric.persist.pojo.FVGRecordDto;
import lombok.Data;

import java.util.List;

@Data
public class FVGObject extends FVGRecordDto {

    private Quote latestQuote;

    private Quote highestQuote;

    private Quote lowestQuote;

    private double latestProfit;

    private double highestProfit;

    private double lowestProfit;

    private String maSorting;

    private String category;

    private double bayesianTrend;

    private String closePrices; ;



    public static FVGObject of(FVGRecordDto recordDto) {
        FVGObject object = new FVGObject();
        object.setClose(recordDto.getClose());
        object.setPosition(recordDto.getPosition());
        object.setId(recordDto.getId());
        object.setName(recordDto.getName());
        object.setDownAvg(recordDto.getDownAvg());
        object.setUpAvg(recordDto.getUpAvg());
        object.setTradeDate(recordDto.getTradeDate());
        return object;
    }

}
