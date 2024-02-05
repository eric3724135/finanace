package com.eric.domain;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.Objects;

import static com.eric.domain.SymbolType.TWE;

@Data
public class Symbol {

    private String id;

    private String name;

    private SymbolType type = TWE;

    public Symbol(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Symbol() {
    }

    public static Symbol of(JsonNode node) {
        Symbol symbol = new Symbol();
        symbol.setId(node.get("No").textValue());
        symbol.setName(node.get("Name").textValue());
        return symbol;
    }

    public static Symbol ofTWE(String id,String name){
        return new Symbol(id,name);
    }

    public static Symbol ofUS(String id,String name){
        Symbol symbol = new Symbol(id,name);
        symbol.setType(SymbolType.US);
        return symbol;
    }

    public static Symbol ofTW(String id,String name){
        Symbol symbol = new Symbol(id+".tw",name);
        symbol.setType(TWE);
        return symbol;
    }

    public static Symbol ofTWO(String id,String name){
        Symbol symbol = new Symbol(id+".two",name);
        symbol.setType(TWE);
        return symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol = (Symbol) o;
        return Objects.equals(id, symbol.id) &&
                Objects.equals(name, symbol.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
