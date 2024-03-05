package com.eric.wessiorfinance;

import com.eric.domain.Symbol;
import com.eric.parser.ParserError;
import com.eric.parser.ParserResult;
import com.eric.utils.JsonUtils;
import com.eric.wessiorfinance.util.WessiorFintechTunnel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

@Slf4j
public class WFTunnelDataHandler extends WFAbstractParser<WessiorFintechTunnel> {

    //    https://invest.wessiorfinance.com/stock_api/Big_Trend?Stock=2404&Odate=2021-06-16&Period=3.5
    private static final String URL_TEMPLATE = "https://invest.wessiorfinance.com/stock_api/Big_Trend?Stock=%s&Odate=%s&Period=3.5";

    private Symbol symbol;
    private LocalDate date;
    private String dateStr;

    public WFTunnelDataHandler(Symbol symbol, LocalDate date) {
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
        ParserResult<WessiorFintechTunnel> result = new ParserResult<>();
        String jsonStr;
        try {
            jsonStr = this.getResponse().body();
            ObjectMapper mapper = JsonUtils.getMapper();
            JsonNode jsonNode = mapper.readValue(jsonStr, JsonNode.class);
            Iterator<JsonNode> elements = jsonNode.iterator();
            while (elements.hasNext()) {
                JsonNode node = elements.next();
                try {
                    WessiorFintechTunnel tl = WessiorFintechTunnel.of(symbol, node);
                    result.getResultList().add(tl);
                } catch (Exception e) {
                }
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
