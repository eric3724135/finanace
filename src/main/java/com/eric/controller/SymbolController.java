package com.eric.controller;

import com.eric.domain.Symbol;
import com.eric.service.SymbolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class SymbolController {

    @Autowired
    private SymbolService symbolService;
    @GetMapping("/symbol")
    public String fetchSymbol(Model model) {
        List<Symbol> symbolList = symbolService.getTWESymbols();

        return "index";
    }
}
