package com.gis.service;

import com.gis.entity.Division;
import org.springframework.lang.NonNull;

import java.util.List;

public interface DivisionService {
    
    // 获取所有区划
    List<Division> getAllDivisions();
    
    // 根据ID获取区划
    Division getDivisionById(@NonNull Long id);
    
    // 根据级别获取区划
    List<Division> getDivisionsByLevel(Integer level);
    
    // 根据父级ID获取子级区划
    List<Division> getDivisionsByParentId(@NonNull Long parentId);
    
    // 根据父级ID和级别获取区划
    List<Division> getDivisionsByParentIdAndLevel(@NonNull Long parentId, Integer level);
    
    // 保存区划
    Division saveDivision(@NonNull Division division);
    
    // 删除区划
    void deleteDivision(@NonNull Long id);
}