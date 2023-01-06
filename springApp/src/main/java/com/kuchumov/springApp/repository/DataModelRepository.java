package com.kuchumov.springApp.repository;

import com.kuchumov.springApp.entity.DataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DataModelRepository extends JpaRepository<DataModel, Long> {

    @Query("select d.name from DataModel d")
    List<String> getAllNames();

}
