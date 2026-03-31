package com.gis.repository;

import com.gis.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByStatus(Integer status);
    List<Alarm> findAllByOrderByAlarmTimeDesc();
    List<Alarm> findByStatusOrderByAlarmTimeDesc(Integer status);
}
