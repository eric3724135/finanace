package com.eric.histock.util;

import com.eric.domain.CMQuote;
import com.eric.domain.Period;
import com.eric.domain.Symbol;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class HiStockTechDataParser {

    public static List<CMQuote> parse(Symbol symbol, JsonNode jsonNode) throws JsonProcessingException {
        Map<LocalDateTime, CMQuote> quoteMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        Period period = Period.ONE_DAY;

        JsonNode node = jsonNode.get("DailyK");
        ArrayNode arrayNode = mapper.readValue(node.asText(), ArrayNode.class);
        HiStockTechDataParser.parseDailyK(quoteMap, arrayNode, symbol, period);

        node = jsonNode.get("Volume");
        arrayNode = mapper.readValue(node.asText(), ArrayNode.class);
        HiStockTechDataParser.parseVolume(quoteMap, arrayNode, symbol, period);

        node = jsonNode.get("Mean5");
        arrayNode = mapper.readValue(node.asText(), ArrayNode.class);
        HiStockTechDataParser.parseMa5(quoteMap, arrayNode, symbol, period);

        node = jsonNode.get("Mean10");
        arrayNode = mapper.readValue(node.asText(), ArrayNode.class);
        HiStockTechDataParser.parseMa10(quoteMap, arrayNode, symbol, period);

        node = jsonNode.get("Mean20");
        arrayNode = mapper.readValue(node.asText(), ArrayNode.class);
        HiStockTechDataParser.parseMa20(quoteMap, arrayNode, symbol, period);

        node = jsonNode.get("Mean60");
        arrayNode = mapper.readValue(node.asText(), ArrayNode.class);
        HiStockTechDataParser.parseMa60(quoteMap, arrayNode, symbol, period);

        node = jsonNode.get("Mean120");
        arrayNode = mapper.readValue(node.asText(), ArrayNode.class);
        HiStockTechDataParser.parseMa120(quoteMap, arrayNode, symbol, period);

        node = jsonNode.get("K9");
        arrayNode = mapper.readValue(node.asText(), ArrayNode.class);
        HiStockTechDataParser.parseK9(quoteMap, arrayNode, symbol, period);

        node = jsonNode.get("D9");
        arrayNode = mapper.readValue(node.asText(), ArrayNode.class);
        HiStockTechDataParser.parseD9(quoteMap, arrayNode, symbol, period);

        node = jsonNode.get("RSI6");
        arrayNode = mapper.readValue(node.asText(), ArrayNode.class);
        HiStockTechDataParser.parseRSI6(quoteMap, arrayNode, symbol, period);

        node = jsonNode.get("RSI12");
        arrayNode = mapper.readValue(node.asText(), ArrayNode.class);
        HiStockTechDataParser.parseRSI12(quoteMap, arrayNode, symbol, period);

        node = jsonNode.get("DIF");
        arrayNode = mapper.readValue(node.asText(), ArrayNode.class);
        HiStockTechDataParser.parseMACDDIF(quoteMap, arrayNode, symbol, period);

        node = jsonNode.get("MACD");
        arrayNode = mapper.readValue(node.asText(), ArrayNode.class);
        HiStockTechDataParser.parseMACD(quoteMap, arrayNode, symbol, period);

        node = jsonNode.get("OSC");
        arrayNode = mapper.readValue(node.asText(), ArrayNode.class);
        HiStockTechDataParser.parseOSC(quoteMap, arrayNode, symbol, period);

        fillDiff(quoteMap);

        return new ArrayList<>(quoteMap.values());
    }

    private static void parseDailyK(Map<LocalDateTime, CMQuote> quoteMap, ArrayNode arrayNode, Symbol symbol, Period period) {
        for (JsonNode node : arrayNode) {
            ArrayNode dataArray = (ArrayNode) node;
            long millis = dataArray.get(0).asLong();
            CMQuote quote = getQuoteByTime(quoteMap, symbol, period, millis);
            quote.setOpen(dataArray.get(1).asDouble());
            quote.setHigh(dataArray.get(2).asDouble());
            quote.setLow(dataArray.get(3).asDouble());
            quote.setClose(dataArray.get(4).asDouble());
        }
    }

    private static void parseVolume(Map<LocalDateTime, CMQuote> quoteMap, ArrayNode arrayNode, Symbol symbol, Period period) {
        for (JsonNode node : arrayNode) {
            ArrayNode dataArray = (ArrayNode) node;
            long millis = dataArray.get(0).asLong();
            CMQuote quote = getQuoteByTime(quoteMap, symbol, period, millis);
            quote.setVolume(dataArray.get(1).asDouble());
        }
    }

    private static void parseMa5(Map<LocalDateTime, CMQuote> quoteMap, ArrayNode arrayNode, Symbol symbol, Period period) {
        for (JsonNode node : arrayNode) {
            ArrayNode dataArray = (ArrayNode) node;
            long millis = dataArray.get(0).asLong();
            CMQuote quote = getQuoteByTime(quoteMap, symbol, period, millis);
            quote.setMa5(dataArray.get(1).asDouble());
        }
    }

    private static void parseMa10(Map<LocalDateTime, CMQuote> quoteMap, ArrayNode arrayNode, Symbol symbol, Period period) {
        for (JsonNode node : arrayNode) {
            ArrayNode dataArray = (ArrayNode) node;
            long millis = dataArray.get(0).asLong();
            CMQuote quote = getQuoteByTime(quoteMap, symbol, period, millis);
            try {
                quote.setMa10(dataArray.get(1).asDouble());
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    private static void parseMa20(Map<LocalDateTime, CMQuote> quoteMap, ArrayNode arrayNode, Symbol symbol, Period period) {
        for (JsonNode node : arrayNode) {
            ArrayNode dataArray = (ArrayNode) node;
            long millis = dataArray.get(0).asLong();
            CMQuote quote = getQuoteByTime(quoteMap, symbol, period, millis);
            quote.setMa20(dataArray.get(1).asDouble());
        }
    }

    private static void parseMa60(Map<LocalDateTime, CMQuote> quoteMap, ArrayNode arrayNode, Symbol symbol, Period period) {
        for (JsonNode node : arrayNode) {
            ArrayNode dataArray = (ArrayNode) node;
            long millis = dataArray.get(0).asLong();
            CMQuote quote = getQuoteByTime(quoteMap, symbol, period, millis);
            quote.setMa60(dataArray.get(1).asDouble());
        }
    }

    private static void parseMa120(Map<LocalDateTime, CMQuote> quoteMap, ArrayNode arrayNode, Symbol symbol, Period period) {
        for (JsonNode node : arrayNode) {
            ArrayNode dataArray = (ArrayNode) node;
            long millis = dataArray.get(0).asLong();
            CMQuote quote = getQuoteByTime(quoteMap, symbol, period, millis);
            quote.setMa120(dataArray.get(1).asDouble());
        }
    }

    private static void parseK9(Map<LocalDateTime, CMQuote> quoteMap, ArrayNode arrayNode, Symbol symbol, Period period) {
        for (JsonNode node : arrayNode) {
            ArrayNode dataArray = (ArrayNode) node;
            long millis = dataArray.get(0).asLong();
            CMQuote quote = getQuoteByTime(quoteMap, symbol, period, millis);
            quote.setK9(dataArray.get(1).asDouble());
        }
    }


    private static void parseD9(Map<LocalDateTime, CMQuote> quoteMap, ArrayNode arrayNode, Symbol symbol, Period period) {
        for (JsonNode node : arrayNode) {
            ArrayNode dataArray = (ArrayNode) node;
            long millis = dataArray.get(0).asLong();
            CMQuote quote = getQuoteByTime(quoteMap, symbol, period, millis);
            quote.setD9(dataArray.get(1).asDouble());
        }
    }

    private static void parseRSI6(Map<LocalDateTime, CMQuote> quoteMap, ArrayNode arrayNode, Symbol symbol, Period period) {
        for (JsonNode node : arrayNode) {
            ArrayNode dataArray = (ArrayNode) node;
            long millis = dataArray.get(0).asLong();
            CMQuote quote = getQuoteByTime(quoteMap, symbol, period, millis);
            quote.setRsi5(dataArray.get(1).asDouble());
        }
    }

    private static void parseRSI12(Map<LocalDateTime, CMQuote> quoteMap, ArrayNode arrayNode, Symbol symbol, Period period) {
        for (JsonNode node : arrayNode) {
            ArrayNode dataArray = (ArrayNode) node;
            long millis = dataArray.get(0).asLong();
            CMQuote quote = getQuoteByTime(quoteMap, symbol, period, millis);
            quote.setRsi10(dataArray.get(1).asDouble());
        }
    }

    private static void parseMACDDIF(Map<LocalDateTime, CMQuote> quoteMap, ArrayNode arrayNode, Symbol symbol, Period period) {
        for (JsonNode node : arrayNode) {
            ArrayNode dataArray = (ArrayNode) node;
            long millis = dataArray.get(0).asLong();
            CMQuote quote = getQuoteByTime(quoteMap, symbol, period, millis);
            quote.setMacdDiff(dataArray.get(1).asDouble());
        }
    }

    private static void parseMACD(Map<LocalDateTime, CMQuote> quoteMap, ArrayNode arrayNode, Symbol symbol, Period period) {
        for (JsonNode node : arrayNode) {
            ArrayNode dataArray = (ArrayNode) node;
            long millis = dataArray.get(0).asLong();
            CMQuote quote = getQuoteByTime(quoteMap, symbol, period, millis);
            quote.setMacd(dataArray.get(1).asDouble());
        }
    }
    private static void parseOSC(Map<LocalDateTime, CMQuote> quoteMap, ArrayNode arrayNode, Symbol symbol, Period period) {
        for (JsonNode node : arrayNode) {
            ArrayNode dataArray = (ArrayNode) node;
            long millis = dataArray.get(0).asLong();
            CMQuote quote = getQuoteByTime(quoteMap, symbol, period, millis);
            quote.setOsc(dataArray.get(1).asDouble());
        }
    }


    private static CMQuote getQuoteByTime(Map<LocalDateTime, CMQuote> quoteMap, Symbol symbol, Period period, long millis) {
        //
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(millis),
                        TimeZone.getDefault().toZoneId());
        localDateTime = localDateTime.toLocalDate().atStartOfDay();
        CMQuote quote = quoteMap.computeIfAbsent(localDateTime, dateTime -> {
            CMQuote newQuote = new CMQuote();
            newQuote.setSymbol(symbol.getId());
            newQuote.setName(symbol.getName());
            newQuote.setPeriod(period.getCode());
            newQuote.setTradeDate(dateTime);
            return newQuote;
        });
        return quote;
    }

    private static void fillDiff(Map<LocalDateTime, CMQuote> quoteMap) {
        List<CMQuote> list = new ArrayList<>(quoteMap.values());
        list.sort(Comparator.comparing(CMQuote::getTradeDate).reversed());
        for (int i = 0; i < list.size() - 1; i++) {
            CMQuote today = list.get(i);
            CMQuote past = list.get(i + 1);
            CMQuote quote = quoteMap.get(today.getTradeDate());
            quote.setDiff(today.getClose() - past.getClose());
        }
    }


    public static void main(String[] args) throws JsonProcessingException {
        String source = "{\"DailyK\":\"[[1588550400000,294.5,296.5,294,295],[1588636800000,296.5,298,295,295.5],[1588723200000,294.5,296,292.5,296],[1588809600000,294.5,299,294.5,297.5],[1588896000000,300,300,296,297.5],[1589155200000,300,301.5,298.5,301],[1589241600000,297.5,298.5,295,295],[1589328000000,293,297,292.5,297]]\",\"Close\":\"[[1588550400000,295],[1588636800000,295.5],[1588723200000,296],[1588809600000,297.5],[1588896000000,297.5],[1589155200000,301],[1589241600000,295],[1589328000000,297]]\",\"Volume\":\"[[1588550400000,71472],[1588636800000,23421],[1588723200000,34182],[1588809600000,27563],[1588896000000,32199],[1589155200000,28669],[1589241600000,51969],[1589328000000,25978]]\",\"Mean5\":\"[[1588550400000,298.6],[1588636800000,298.1],[1588723200000,298],[1588809600000,297.7],[1588896000000,296.3],[1589155200000,297.5],[1589241600000,297.4],[1589328000000,297.6]]\",\"Mean10\":\"[[1588550400000,297.55],[1588636800000,296.7],[1588723200000,296.8],[1588809600000,297.15],[1588896000000,297.35],[1589155200000,298.05],[1589241600000,297.75],[1589328000000,297.8]]\",\"Mean20\":\"[[1588550400000,291.275],[1588636800000,292.275],[1588723200000,292.925],[1588809600000,293.55],[1588896000000,294.275],[1589155200000,295.35],[1589241600000,296.175],[1589328000000,296.775]]\",\"Mean60\":\"[[1588550400000,299.3],[1588636800000,298.767],[1588723200000,298.158],[1588809600000,297.65],[1588896000000,297.15],[1589155200000,296.642],[1589241600000,295.975],[1589328000000,295.342]]\",\"Mean120\":\"[[1588550400000,310.5],[1588636800000,310.471],[1588723200000,310.379],[1588809600000,310.271],[1588896000000,310.158],[1589155200000,310.092],[1589241600000,310.004],[1589328000000,309.971]]\",\"Mean5Volume\":\"[[1588550400000,49249],[1588636800000,46243],[1588723200000,45618],[1588809600000,42335],[1588896000000,37767],[1589155200000,29206],[1589241600000,34916],[1589328000000,33275]]\",\"Mean20Volume\":\"[[1588550400000,47190],[1588636800000,45545],[1588723200000,44819],[1588809600000,44311],[1588896000000,44543],[1589155200000,44618],[1589241600000,46110],[1589328000000,44971]]\",\"K9\":\"[[1588550400000,50.5802],[1588636800000,45.2144],[1588723200000,39.4762],[1588809600000,39.6508],[1588896000000,39.7672],[1589155200000,49.1781],[1589241600000,39.4521],[1589328000000,38.3014]]\",\"D9\":\"[[1588550400000,56.3208],[1588636800000,52.6187],[1588723200000,48.2379],[1588809600000,45.3755],[1588896000000,43.5061],[1589155200000,45.3968],[1589241600000,43.4152],[1589328000000,41.7106]]\",\"RSI6\":\"[[1588550400000,48.1284],[1588636800000,49.1841],[1588723200000,50.3957],[1588809600000,54.3167],[1588896000000,54.3167],[1589155200000,63.9037],[1589241600000,44.6346],[1589328000000,50.5937]]\",\"RSI12\":\"[[1588550400000,52.2342],[1588636800000,52.6622],[1588723200000,53.1204],[1588809600000,54.56],[1588896000000,54.56],[1589155200000,58.1303],[1589241600000,50.683],[1589328000000,52.8782]]\",\"DIF\":\"[[1588550400000,3.04947],[1588636800000,2.86139],[1588723200000,2.61166],[1588809600000,2.54584],[1588896000000,2.51508],[1589155200000,2.6817],[1589241600000,2.41272],[1589328000000,2.1745]]\",\"MACD\":\"[[1588550400000,1.6953],[1588636800000,1.92852],[1588723200000,2.06515],[1588809600000,2.16129],[1588896000000,2.23205],[1589155200000,2.32198],[1589241600000,2.34013],[1589328000000,2.307]]\",\"OSC\":\"[[1588550400000,1.35416],[1588636800000,0.932868],[1588723200000,0.546514],[1588809600000,0.38455],[1588896000000,0.28303],[1589155200000,0.359725],[1589241600000,0.0725944],[1589328000000,-0.132501]]\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(source, JsonNode.class);
        Symbol symbol = new Symbol();
        symbol.setId("2330");
        symbol.setName("台積電");
        List<CMQuote> quotes = HiStockTechDataParser.parse(symbol, node);
        log.info(quotes.size() + "");
    }
}
