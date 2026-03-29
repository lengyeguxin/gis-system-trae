package com.gis.service;

import com.gis.entity.Division;

import java.util.List;

public interface DivisionService {
    
    // 获取所有区划
    List<Division> getAllDivisions();
    
    // 根据ID获取区划
    Division getDivisionById(Long id);
    
    // 根据级别获取区划
    List<Division> getDivisionsByLevel(Integer level);
    
    // 根据父级ID获取子级区划
    List<Division> getDivisionsByParentId(Long parentId);
    
    // 根据父级ID和级别获取区划
    List<Division> getDivisionsByParentIdAndLevel(Long parentId, Integer level);
    
    // 保存区划
    Division saveDivision(Division division);
    
    // 删除区划
    void deleteDivision(Long id);
}