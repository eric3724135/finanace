package com.eric.persist.pojo;

import com.eric.domain.Symbol;
import com.eric.domain.SymbolType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Entity
@Table(name = "favorite_symbol")
public class FavoriteSymbolDto {

    @Id
    @Column(name = "id")
    String id;
    @Column(name = "name")
    String name;
    //0: 台股 1: 美股
    @Column(name = "type")
    String type;

    public FavoriteSymbolDto() {
    }

    public FavoriteSymbolDto(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public Symbol getSymbolObj(){
        Symbol symbol = new Symbol(id,name);
        symbol.setType(SymbolType.getByCode(type));
        return symbol;
    }
}
