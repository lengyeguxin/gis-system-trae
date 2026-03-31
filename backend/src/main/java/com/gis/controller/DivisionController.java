package com.gis.controller;

import com.gis.entity.Division;
import com.gis.service.DivisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.lang.NonNull;

import java.util.List;

@RestController
@RequestMapping("/api/division")
public class DivisionController {
    
    @Autowired
    private DivisionService divisionService;
    
    // 获取所有区划
    @GetMapping("/all")
    public List<Division> getAllDivisions() {
        return divisionService.getAllDivisions();
    }
    
    // 根据ID获取区划
    @GetMapping("/id/{id}")
    public Division getDivisionById(@PathVariable @NonNull Long id) {
        return divisionService.getDivisionById(id);
    }
    
    // 根据级别获取区划
    @GetMapping("/level/{level}")
    public List<Division> getDivisionsByLevel(@PathVariable Integer level) {
        return divisionService.getDivisionsByLevel(level);
    }
    
    // 根据父级ID获取子级区划
    @GetMapping("/parent/{parentId}")
    public List<Division> getDivisionsByParentId(@PathVariable @NonNull Long parentId) {
        return divisionService.getDivisionsByParentId(parentId);
    }
    
    // 根据父级ID和级别获取区划
    @GetMapping("/parent/{parentId}/level/{level}")
    public List<Division> getDivisionsByParentIdAndLevel(@PathVariable @NonNull Long parentId, @PathVariable Integer level) {
        return divisionService.getDivisionsByParentIdAndLevel(parentId, level);
    }
}