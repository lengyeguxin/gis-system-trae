package com.gis.repository;

import com.gis.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByStatus(Integer status);
    
    @Query("SELECT a FROM t_alarm a ORDER BY a.alarm_time DESC")
    List<Alarm> findAllOrderByAlarmTimeDesc();
}
