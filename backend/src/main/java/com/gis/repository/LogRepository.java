package com.gis.repository;

import com.gis.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {
    List<Log> findByUsername(String username);
    List<Log> findByOperation(String operation);
    List<Log> findByCreateTimeBetween(Date startDate, Date endDate);
}
