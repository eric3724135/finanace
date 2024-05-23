package com.eric.persist.repo;

import com.eric.persist.pojo.FavoriteSymbolDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface FavoriteSymbolRepository extends JpaRepository<FavoriteSymbolDto, String> {

    @Query(value = "SELECT * from favorite_symbol  where type = ?1", nativeQuery = true)
    List<FavoriteSymbolDto> findByType(String type);

}
