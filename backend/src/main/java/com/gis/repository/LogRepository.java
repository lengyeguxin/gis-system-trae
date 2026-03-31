package com.gis.repository;

import com.gis.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {
    List<Log> findByUsername(String username);
    List<Log> findByOperation(String operation);
    List<Log> findByCreateTimeBetween(Date startDate, Date endDate);
    
    @Query("SELECT l FROM Log l ORDER BY l.createTime DESC")
    List<Log> findAllOrderByCreateTimeDesc();
    
    @Query("SELECT l FROM Log l WHERE l.username = :username ORDER BY l.createTime DESC")
    List<Log> findByUsernameOrderByCreateTimeDesc(@Param("username") String username);
    
    @Query("SELECT l FROM Log l WHERE l.operation = :operation ORDER BY l.createTime DESC")
    List<Log> findByOperationOrderByCreateTimeDesc(@Param("operation") String operation);
    
    @Query("SELECT l FROM Log l WHERE l.createTime BETWEEN :startDate AND :endDate ORDER BY l.createTime DESC")
    List<Log> findByCreateTimeBetweenOrderByCreateTimeDesc(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
