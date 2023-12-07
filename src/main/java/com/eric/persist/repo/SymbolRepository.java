package com.eric.persist.repo;

import com.eric.persist.pojo.SymbolDto;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SymbolRepository extends JpaRepository<SymbolDto, String> {


}
