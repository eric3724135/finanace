package com.eric.histock;

import com.eric.domain.CMQuote;
import com.eric.domain.Period;
import com.eric.domain.Symbol;
import com.eric.histock.util.HiStockTechDataParser;
import com.eric.parser.ParserError;
import com.eric.parser.ParserResult;
import com.eric.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class HiStockDataHandler extends HiStockAbstractParser<CMQuote> {

    //https://histock.tw/stock/chip/chartdata.aspx?no=2330&days=80&m=dailyk,close,volume,mean5,mean10,mean20,mean60,mean120,mean5volume,mean20volume,k9,d9,rsi6,rsi12,dif,macd,osc

    private Period period;

    public HiStockDataHandler(Symbol symbol, Period period, int days) {
        super(symbol, days);
        this.period = period;
    }


    @Override
    public ParserResult getResult() {
        ParserResult<CMQuote> result = new ParserResult<>();
        String jsonStr;
        try {
            jsonStr = this.getResponse().body();
            ObjectMapper mapper = JsonUtils.getMapper();
            JsonNode jsonNode = mapper.readValue(jsonStr, JsonNode.class);
            List<CMQuote> list = HiStockTechDataParser.parse(getSymbol(), jsonNode);
            result.setSuccess(true);
            result.getResultList().addAll(list);
//            log.debug("[{}] 解析成功 ", getSymbol());
            return result;
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(ParserError.JSOUP_PARSE_ERROR);
            log.error("", e);

        }
        return result;
    }
}
