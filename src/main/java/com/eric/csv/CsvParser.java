package com.eric.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public abstract class CsvParser<T> {

    abstract List<T> parserFile() throws Exception;

    public CSVParser parse(InputStream inputStream, String... headers) throws IOException {
        CSVParser csvParser = CSVFormat.DEFAULT.withHeader(headers).parse(new InputStreamReader(inputStream));
        return csvParser;
//        for (CSVRecord record : csvParser) {
//            String field_1 = record.get(0);
//            String field_2 = record.get(1);
//        }
    }
}
