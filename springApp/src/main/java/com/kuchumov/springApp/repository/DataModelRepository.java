package com.kuchumov.springApp.repository;

import com.kuchumov.springApp.entity.DataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DataModelRepository extends JpaRepository<DataModel, Long> {

    @Query("select d.name from DataModel d")
    List<String> getAllNames();

    @Query("select d from DataModel d " +
            "where (d.changeDate between :dateFrom and :dateTo) " +
            "and (d.name like %:name%)" +
            "and (d.type like %:type%)")
    List<DataModel> getAllModelsFilterByNameAndDateAndType(String name, Date dateFrom, Date dateTo, String type);


    @Modifying
    @Query("update DataModel d " +
            "set d.comment = :comment, d.changeDate = :changeDate " +
            "where d.id = :id")
    void updateCommentById(Long id, String comment, Date changeDate);

    @Modifying
    @Query("delete from DataModel d " +
            "where d.id = :id")
    void deleteDataModelById(Long id);
}
