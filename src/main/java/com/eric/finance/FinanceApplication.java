package com.eric.finance;

import com.eric.domain.Symbol;
import com.eric.service.SymbolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.util.List;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.eric"})
@EntityScan("com.eric.persist")
public class FinanceApplication implements CommandLineRunner {

    @Autowired
    private SymbolService symbolService;

    public static void main(String[] args) {
        SpringApplication.run(FinanceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        boolean tweSymbolExist = symbolService.existTWESymbol();
        if(!tweSymbolExist){
            //symbol table 無資料重撈data
            List<Symbol> list = symbolService.getTWESymbols();
            list.parallelStream().forEach(data -> {
                symbolService.addSymbol(data);
            });
        }
        boolean usSymbolExist = symbolService.existUSSymbol();
        if(!usSymbolExist){
            //symbol table 無資料重撈data
            List<Symbol> list = symbolService.getUSSymbols();
            list.parallelStream().forEach(data -> {
                symbolService.addSymbol(data);
            });
        }
        //symbol table 有資料不重撈
        log.info("START");
    }


}
