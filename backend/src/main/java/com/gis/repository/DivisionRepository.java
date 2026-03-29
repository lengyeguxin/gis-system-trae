package com.gis.repository;

import com.gis.entity.Division;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Long> {
    
    // 根据级别查询区划
    List<Division> findByLevel(Integer level);
    
    // 根据父级ID查询子级区划
    List<Division> findByParentId(Long parentId);
    
    // 根据父级ID和级别查询区划
    List<Division> findByParentIdAndLevel(Long parentId, Integer level);
}