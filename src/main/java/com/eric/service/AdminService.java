package com.eric.service;

import com.eric.persist.repo.QuoteRepository;
import com.eric.persist.repo.SymbolRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminService {
    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private SymbolRepository symbolRepository;

    public void truncateQuote() {
        quoteRepository.deleteAll();
    }

    public void truncateSymbol() {
        symbolRepository.deleteAll();
    }
}
