package com.eric.excel;

import com.eric.domain.FVGPosition;
import com.eric.persist.pojo.FVGRecordDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FVGStrategyExcelHandler {

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public FVGStrategyExcelHandler() {

    }

    public ByteArrayOutputStream export(List<FVGRecordDto> fvgRecords) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(); // 建立Excel物件
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        List<FVGRecordDto> buyList = new ArrayList<>();
        List<FVGRecordDto> sellList = new ArrayList<>();

        for(FVGRecordDto fvgRecord : fvgRecords){
            if(FVGPosition.BUY.name().equals(fvgRecord.getPosition())){
                buyList.add(fvgRecord);
            }
            if(FVGPosition.SELL.name().equals(fvgRecord.getPosition())){
                sellList.add(fvgRecord);
            }
        }

        XSSFSheet buySheet = workbook.createSheet("買入");
        buySheet.autoSizeColumn(0); // 自動調整欄位寬度
        XSSFRow row = buySheet.createRow(0);
        buySheet.setColumnWidth(0, 3000);
        buySheet.setColumnWidth(1, 3000);
        buySheet.setColumnWidth(2, 3000);
        buySheet.setColumnWidth(3, 3000);
        buySheet.setColumnWidth(4, 3000);
        row.createCell(0).setCellValue("日期");
        row.createCell(1).setCellValue("代號");
        row.createCell(2).setCellValue("名稱");
        row.createCell(3).setCellValue("收盤價");
        row.createCell(4).setCellValue("建議");


        int num = 1;
        for (FVGRecordDto fvgRecord : fvgRecords) {
            row = buySheet.createRow(num++);
            row.createCell(0).setCellValue(fvgRecord.getTradeDate().format(dateFormatter));
            row.createCell(1).setCellValue(fvgRecord.getId());
            row.createCell(2).setCellValue(fvgRecord.getName());
            row.createCell(3).setCellValue(fvgRecord.getClose());
            row.createCell(4).setCellValue(fvgRecord.getPosition());

        }

        XSSFSheet sellSheet = workbook.createSheet("售出");
        sellSheet.autoSizeColumn(0); // 自動調整欄位寬度
        row = sellSheet.createRow(0);
        sellSheet.setColumnWidth(0, 3000);
        sellSheet.setColumnWidth(1, 3000);
        sellSheet.setColumnWidth(2, 3000);
        sellSheet.setColumnWidth(3, 3000);
        sellSheet.setColumnWidth(4, 3000);
        row.createCell(0).setCellValue("日期");
        row.createCell(1).setCellValue("代號");
        row.createCell(2).setCellValue("名稱");
        row.createCell(3).setCellValue("收盤價");
        row.createCell(4).setCellValue("建議");


        num = 1;
        for (FVGRecordDto fvgRecord : fvgRecords) {
            row = sellSheet.createRow(num++);
            row.createCell(0).setCellValue(fvgRecord.getTradeDate().format(dateFormatter));
            row.createCell(1).setCellValue(fvgRecord.getId());
            row.createCell(2).setCellValue(fvgRecord.getName());
            row.createCell(3).setCellValue(fvgRecord.getClose());
            row.createCell(4).setCellValue(fvgRecord.getPosition());

        }

        workbook.write(bos);
        workbook.close();

        return bos;
    }
}
