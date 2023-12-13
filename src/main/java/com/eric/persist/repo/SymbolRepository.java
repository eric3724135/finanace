package com.eric.persist.repo;

import com.eric.persist.pojo.SymbolDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface SymbolRepository extends JpaRepository<SymbolDto, String> {

    @Query(value = "SELECT * from symbol  where type = ?1", nativeQuery = true)
    List<SymbolDto> findByType(String type);

}
