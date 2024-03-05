package com.eric.wessiorfinance;

import com.eric.domain.Symbol;
import com.eric.parser.ParserError;
import com.eric.parser.ParserResult;
import com.eric.utils.JsonUtils;
import com.eric.wessiorfinance.util.WessiorFintechTL;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

@Slf4j
public class WFTLDataHandler extends WFAbstractParser<WessiorFintechTL> {

    //https://invest.wessiorfinance.com/Stock_api/Notation_cal?Stock=2404&Odate=2021-06-04&Period=3.5&is_log=0&is_adjclose=0
    private static final String URL_TEMPLATE = "https://invest.wessiorfinance.com/Stock_api/Notation_cal?Stock=%s&Odate=%s&Period=3.5&is_log=0&is_adjclose=0";

    private Symbol symbol;
    private LocalDate date;
    private String dateStr;

    public WFTLDataHandler(Symbol symbol, LocalDate date) {
        this.symbol = symbol;
        this.date = date;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.dateStr = formatter.format(date);
    }

    @Override
    public String getUrl() {
        return String.format(URL_TEMPLATE, symbol.getId(), dateStr);
    }

    @Override
    public ParserResult getResult() {
        ParserResult<WessiorFintechTL> result = new ParserResult<>();
        String jsonStr;
        try {
            jsonStr = this.getResponse().body();
            ObjectMapper mapper = JsonUtils.getMapper();
            JsonNode jsonNode = mapper.readValue(jsonStr, JsonNode.class);
            for (JsonNode node : jsonNode) {
                WessiorFintechTL tl = WessiorFintechTL.of(symbol, node);
                result.getResultList().add(tl);
            }


            result.setSuccess(true);
            log.debug("[{}] 解析成功  ", symbol.getId());
            return result;
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(ParserError.JSOUP_PARSE_ERROR);
            log.error("", e);

        }
        return result;
    }
}
