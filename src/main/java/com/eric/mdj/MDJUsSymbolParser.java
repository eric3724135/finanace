package com.eric.mdj;

import com.eric.domain.Symbol;
import com.eric.domain.UsSymbol;
import com.eric.parser.ParserError;
import com.eric.parser.ParserResult;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


@Slf4j
public class MDJUsSymbolParser extends MDJStockAbstractParser<Symbol> {
    public MDJUsSymbolParser() {
        super();
    }


    @Override
    public ParserResult<Symbol> getResult() {
        ParserResult<Symbol> result = new ParserResult<>();
        Connection.Response response;
        try {
            response = this.getResponse();
            Elements elements = response.parse().select("table#oMainTable > tbody > tr");
            for (Element element : elements) {
                Elements tdElements = element.select("td");
                String symbol = tdElements.get(3).child(0).text();
                String name = tdElements.get(4).text();
                result.getResultList().add(Symbol.ofUS(symbol,name));
            }
            result.setSuccess(true);
            return result;
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(ParserError.JSOUP_PARSE_ERROR);
            log.error("", e);

        }
        return result;
    }

}
