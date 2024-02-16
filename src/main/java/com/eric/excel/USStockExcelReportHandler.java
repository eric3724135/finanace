package com.eric.excel;

import com.eric.domain.Quote;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

@Slf4j
public class USStockExcelReportHandler {


    public USStockExcelReportHandler() {

    }

    public ByteArrayOutputStream export(List<Quote> quotes) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(); // 建立Excel物件
        CreationHelper createHelper = workbook.getCreationHelper();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //字體格式
        Font redFont = workbook.createFont();
        redFont.setColor(IndexedColors.RED.getIndex());
        // 設定儲存格格式
        CellStyle redFontCellStyle = workbook.createCellStyle();
        redFontCellStyle.setFont(redFont);
        redFontCellStyle.setWrapText(true); // 自動換行

        CellStyle hlink_style = workbook.createCellStyle();
        Font hlink_font = workbook.createFont();
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLUE.getIndex());
        hlink_style.setFont(hlink_font);

        Hyperlink titleHLink = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
        titleHLink.setAddress("'總表'!A1");

        //字體格式
        Font orangeFont = workbook.createFont();
        orangeFont.setColor(IndexedColors.ORANGE.getIndex());
        // 設定儲存格格式
        CellStyle orangeFontCellStyle = workbook.createCellStyle();
        orangeFontCellStyle.setFont(orangeFont);
        orangeFontCellStyle.setWrapText(true); // 自動換行

        DecimalFormat frmt = new DecimalFormat();
        frmt.setMaximumFractionDigits(2);

        XSSFSheet sumSheet = workbook.createSheet("總表");
        sumSheet.autoSizeColumn(0); // 自動調整欄位寬度
        XSSFRow row = sumSheet.createRow(0);
        sumSheet.setColumnWidth(0, 2000);
        sumSheet.setColumnWidth(1, 2500);
        sumSheet.setColumnWidth(2, 3000);
        sumSheet.setColumnWidth(3, 3000);
        sumSheet.setColumnWidth(4, 3000);
        sumSheet.setColumnWidth(5, 3000);
        sumSheet.setColumnWidth(6, 3000);
        sumSheet.setColumnWidth(7, 3000);
        sumSheet.setColumnWidth(8, 3000);
        row.createCell(0).setCellValue("日期");
        row.createCell(1).setCellValue("代號");
        row.createCell(2).setCellValue("名稱");
        row.createCell(3).setCellValue("收盤價");
        row.createCell(4).setCellValue("MA120");
        row.createCell(5).setCellValue("P/MA");
        row.createCell(6).setCellValue("RSI6(20以下)");
        row.createCell(7).setCellValue("RSI24");
        row.createCell(8).setCellValue("RSI6(週)");
        row.createCell(9).setCellValue("開盤價");
        row.createCell(10).setCellValue("最高價");
        row.createCell(11).setCellValue("最低價");

        int num = 1;
        for (Quote quote : quotes) {
            row = sumSheet.createRow(num++);
            row.createCell(0).setCellValue(quote.getTradeDateStr());
            row.createCell(1).setCellValue(quote.getSymbol());
            row.createCell(2).setCellValue(quote.getName());
            row.createCell(3).setCellValue(frmt.format(quote.getClose()));
            row.createCell(4).setCellValue(frmt.format(quote.getMa120()));
            double pma = quote.getClose() / quote.getMa120();
            row.createCell(5).setCellValue(frmt.format(pma));
            if (pma < 1) {
                row.getCell(5).setCellStyle(orangeFontCellStyle);
            }
            if (pma < 0.8) {
                row.getCell(5).setCellStyle(redFontCellStyle);
            }
            row.createCell(6).setCellValue(frmt.format(quote.getRsi5()));
            row.createCell(7).setCellValue(frmt.format(quote.getRsi10()));
            row.createCell(8).setCellValue(frmt.format(quote.getKdDiff()));
            if (quote.getKdDiff() < 20) {
                row.getCell(8).setCellStyle(redFontCellStyle);
            }
            row.createCell(9).setCellValue(frmt.format(quote.getOpen()));
            row.createCell(10).setCellValue(frmt.format(quote.getHigh()));
            row.createCell(11).setCellValue(frmt.format(quote.getLow()));

        }

        workbook.write(bos);
        workbook.close();

        return bos;
    }
}
