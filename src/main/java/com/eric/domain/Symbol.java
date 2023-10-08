package com.eric.domain;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.Objects;

import static com.eric.domain.SymbolType.STOCK_EXCHANGE;

@Data
public class Symbol {

    private String id;

    private String name;

    private SymbolType type = STOCK_EXCHANGE;

    public Symbol(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Symbol() {
    }

    public static Symbol of(JsonNode node) {
        Symbol symbol = new Symbol();
        symbol.setId(node.get("CommKey").textValue());
        symbol.setName(node.get("CommName").textValue());
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
