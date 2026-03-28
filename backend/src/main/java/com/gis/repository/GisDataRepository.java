package com.gis.repository;

import com.gis.entity.GisData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GisDataRepository extends JpaRepository<GisData, Long> {
}
