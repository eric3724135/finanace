package com.eric.service;

import com.eric.domain.Symbol;
import com.eric.parser.ParserResult;
import com.eric.wessiorfinance.WFTLDataHandler;
import com.eric.wessiorfinance.WFTunnelDataHandler;
import com.eric.wessiorfinance.util.TLPosition;
import com.eric.wessiorfinance.util.WessiorFintechTL;
import com.eric.wessiorfinance.util.WessiorFintechTunnel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class WessiorFintechService {

    public TLPosition getTLStatus(Symbol symbol, LocalDate date) {

        WFTLDataHandler handler = new WFTLDataHandler(symbol, date);
        ParserResult<WessiorFintechTL> result = handler.getResult();
        List<WessiorFintechTL> list = result.getResultList();

        WessiorFintechTL latestTL = list.get(list.size() - 1);
        WessiorFintechTL pastTL = list.get(list.size() - 8);
        TLPosition position = new TLPosition(latestTL);
        if (latestTL.getTl() > pastTL.getTl()) {
            position.setRising(true);
        }
        return position;
    }

    public WessiorFintechTunnel getWFTunnel(Symbol symbol, LocalDate date) {

        WFTunnelDataHandler handler = new WFTunnelDataHandler(symbol, date);
        ParserResult<WessiorFintechTunnel> result = handler.getResult();
        List<WessiorFintechTunnel> list = result.getResultList();

        list.sort(Comparator
                .comparing(WessiorFintechTunnel::getDate).reversed());

        return list.get(0);
    }


}
