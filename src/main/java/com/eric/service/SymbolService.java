package com.eric.service;

import com.eric.domain.Symbol;
import com.eric.domain.SymbolType;
import com.eric.histock.HiStockSymbolParser;
import com.eric.mdj.MDJUsSymbolParser;
import com.eric.parser.ParserResult;
import com.eric.persist.pojo.SymbolDto;
import com.eric.persist.repo.SymbolRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SymbolService {

    @Autowired
    private SymbolRepository symbolRepository;

    //
    public List<Symbol> getTWESymbols() {
        HiStockSymbolParser parser = new HiStockSymbolParser();
        ParserResult<Symbol> result = parser.getResult();
        return result.getResultList();
    }

    public List<SymbolDto> getSymbolsFromLocal(SymbolType type) {
        return symbolRepository.findByType(type.getCode());
    }

    public List<Symbol> getUSSymbols() {
        MDJUsSymbolParser parser = new MDJUsSymbolParser();
        ParserResult<Symbol> result = parser.getResult();
        return result.getResultList();
    }

    public boolean existTWESymbol() {
        return symbolRepository.existsById("2330");
    }

    public boolean existUSSymbol() {
        return symbolRepository.existsById("AAPL");
    }

    public Symbol getSymbol(String id) {
        Optional<SymbolDto> optional = symbolRepository.findById(id);
        if (!optional.isPresent()) {
            return null;
        }
        SymbolDto symbolDto = optional.get();
        return new Symbol(symbolDto.getId(), symbolDto.getName());
    }

    public SymbolDto addSymbol(Symbol symbol) {
        SymbolDto symbolDto = symbolRepository.save(new SymbolDto(symbol.getId(), symbol.getName(), symbol.getType().getCode()));
        return symbolDto;
    }
}
