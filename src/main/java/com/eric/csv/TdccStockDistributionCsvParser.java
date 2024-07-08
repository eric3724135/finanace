package com.eric.csv;

import com.eric.persist.pojo.TdccStockDistribution;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedInputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TdccStockDistributionCsvParser<T extends TdccStockDistribution> extends CsvParser<TdccStockDistribution> {

    private static final String FILE_URL = "https://smart.tdcc.com.tw/opendata/getOD.ashx?id=1-5";
    private static final String[] HEADERS = {"資料日期", "證券代號", "持股分級", "人數", "股數", "占集保庫存數比例%"};

    @Override
    public List<TdccStockDistribution> parserFile() throws Exception {
        BufferedInputStream in = new BufferedInputStream(new URL(FILE_URL).openStream());
        CSVParser csvParser = parse(in, HEADERS);
//        for (CSVRecord record : csvParser) {
//            log.info("[{}] {} 分級 {} 人數{} 股數 {} 比例 {}", record.get(1), record.get(0), record.get(2), record.get(3), record.get(4), record.get(5));
//            String field_1 = record.get(0);
//            String field_2 = record.get(1);
//        }
        return getResult(csvParser);
    }

    private List<TdccStockDistribution> getResult(CSVParser csvParser) {
        List<TdccStockDistribution> result = new ArrayList<>();
        Map<String, TdccStockDistribution> map = new HashMap<>();
        boolean first = true;
        for (CSVRecord record : csvParser) {
            if (first) {
                first = false;
                continue;
            }
            TdccStockDistribution distribution = map.computeIfAbsent(record.get(1), s -> {
                TdccStockDistribution dis = new TdccStockDistribution();
                dis.setSymbol(s);
                return dis;
            });
            distribution.setSymbol(record.get(1));
            distribution.setDate(LocalDate.parse((record.get(0)), DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay());
            int seq = Integer.parseInt(record.get(2));
            if (seq == 17) {
                continue;
            }
            if (seq < 10) {
                //100張以下
                distribution.setS100down(distribution.getS100down() + Double.parseDouble(record.get(5)));
            }
            if (seq < 11) {
//                    200張以下
                distribution.setS200down(distribution.getS200down() + Double.parseDouble(record.get(5)));
            }
            if (seq < 12) {
//                    400張以下
                distribution.setS400down(distribution.getS400down() + Double.parseDouble(record.get(5)));
            }
            if (seq > 11) {
//                    400張以上
                distribution.setS400up(distribution.getS400up() + Double.parseDouble(record.get(5)));
            }
            if (seq > 12) {
//                    600張以上
                distribution.setS600up(distribution.getS600up() + Double.parseDouble(record.get(5)));
            }
            if (seq > 13) {
//                    800張以上
                distribution.setS800up(distribution.getS800up() + Double.parseDouble(record.get(5)));
            }
            if (seq > 14) {
//                    1000張以上
                distribution.setS1000up(distribution.getS1000up() + Double.parseDouble(record.get(5)));
            }
        }

        return new ArrayList<>(map.values());
    }

}
