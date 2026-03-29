package com.gis.service.impl;

import com.gis.entity.Division;
import com.gis.repository.DivisionRepository;
import com.gis.service.DivisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.util.List;

@Service
public class DivisionServiceImpl implements DivisionService {
    
    @Autowired
    private DivisionRepository divisionRepository;
    
    @Override
    public List<Division> getAllDivisions() {
        return divisionRepository.findAll();
    }
    
    @Override
    public Division getDivisionById(@NonNull Long id) {
        return divisionRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<Division> getDivisionsByLevel(Integer level) {
        return divisionRepository.findByLevel(level);
    }
    
    @Override
    public List<Division> getDivisionsByParentId(@NonNull Long parentId) {
        return divisionRepository.findByParentId(parentId);
    }
    
    @Override
    public List<Division> getDivisionsByParentIdAndLevel(@NonNull Long parentId, Integer level) {
        return divisionRepository.findByParentIdAndLevel(parentId, level);
    }
    
    @Override
    public Division saveDivision(@NonNull Division division) {
        return divisionRepository.save(division);
    }
    
    @Override
    public void deleteDivision(@NonNull Long id) {
        divisionRepository.deleteById(id);
    }
}